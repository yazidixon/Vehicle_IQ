"""
Runs a real, local PostgreSQL server (via the pip-installable `pgserver`
embedded binaries) WITHOUT requiring admin rights or Docker.

Why this exists: this project targets 100% local execution with a real
PostgreSQL database. Docker Desktop could not be started in this environment
and there is no admin access to install PostgreSQL system-wide, so this
script provides the same real Postgres server through user-space binaries.
Anyone re-running this project can use it exactly the same way (see
docs/installation.md) — it's a legitimate alternative local setup, not a
workaround specific to this machine.

Two known issues with the `pgserver` Windows wheel (as of pgserver 0.1.4) are
patched automatically, idempotently, on every run:

1. It binds to a random free TCP port. We monkeypatch its port picker to
   prefer the standard 5432 (falling back to random only if 5432 is taken),
   so DATABASE_URL / application.yml defaults stay valid across restarts.

2. Its Windows binary distribution ships WITHOUT the IANA timezone data
   (share/postgresql/timezone/ is missing), which makes every JDBC/libpq
   connection fail with "FATAL: invalid value for parameter TimeZone" — even
   for "UTC" — because pgjdbc/libpq send a TimeZone startup parameter that
   Postgres cannot validate without that directory. We fetch a complete,
   official Postgres Windows binary distribution (zonky's well-known
   `embedded-postgres-binaries-windows-amd64` Maven artifact, itself
   repackaged from EnterpriseDB's builds) purely to extract its
   `share/timezone` folder — binary tzdata format is stable across Postgres
   major versions — and copy it into pgserver's install once.
"""

import pathlib
import socket
import subprocess
import sys
import time
import urllib.request
import zipfile

import pgserver.postgres_server as pg_internal

PREFERRED_PORT = 5432
_original_find_port = pg_internal.find_suitable_port


def find_fixed_port(address=None):
    address = address or "127.0.0.1"
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            sock.bind((address, PREFERRED_PORT))
        return PREFERRED_PORT
    except OSError:
        print(f"Port {PREFERRED_PORT} unavailable, falling back to a random free port.", flush=True)
        return _original_find_port(address)


pg_internal.find_suitable_port = find_fixed_port

import pgserver  # noqa: E402  (import after monkeypatch is applied)

HERE = pathlib.Path(__file__).parent
DATA_DIR = HERE / "pgdata"
PGINSTALL_SHARE = pathlib.Path(pgserver.__file__).parent / "pginstall" / "share" / "postgresql"
TZDATA_DIR = PGINSTALL_SHARE / "timezone"

ZONKY_JAR_URL = (
    "https://repo.maven.apache.org/maven2/io/zonky/test/postgres/"
    "embedded-postgres-binaries-windows-amd64/18.6.0/"
    "embedded-postgres-binaries-windows-amd64-18.6.0.jar"
)


def ensure_timezone_data() -> None:
    if TZDATA_DIR.exists() and any(TZDATA_DIR.iterdir()):
        return  # already patched (idempotent)

    print("pgserver's Windows build is missing IANA timezone data — fetching it once...", flush=True)
    work_dir = HERE / "pgbin"
    work_dir.mkdir(exist_ok=True)
    jar_path = work_dir / "pg-windows.jar"
    extract_dir = work_dir / "extracted"

    if not jar_path.exists():
        urllib.request.urlretrieve(ZONKY_JAR_URL, jar_path)

    if not (extract_dir / "share" / "timezone").exists():
        extract_dir.mkdir(exist_ok=True)
        with zipfile.ZipFile(jar_path) as zf:
            zf.extractall(extract_dir)
        txz_files = list(extract_dir.glob("*.txz"))
        if not txz_files:
            raise RuntimeError("Expected a .txz payload inside the zonky jar, found none.")
        subprocess.run(["tar", "-xf", str(txz_files[0])], cwd=extract_dir, check=True)

    import shutil
    shutil.copytree(extract_dir / "share" / "timezone", TZDATA_DIR)
    print(f"Timezone data installed at {TZDATA_DIR}", flush=True)


ensure_timezone_data()

DATA_DIR.mkdir(exist_ok=True)

db = pgserver.get_server(str(DATA_DIR))
print("DEFAULT_URI:", db.get_uri(), flush=True)

setup_sql = """
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'vehicle_admin') THEN
      CREATE ROLE vehicle_admin LOGIN PASSWORD 'change_me_locally' SUPERUSER;
   END IF;
END
$$;
"""
print(db.psql(setup_sql), flush=True)

for dbname in ["vehicle_platform", "vehicle_platform_test"]:
    check = db.psql(f"SELECT 1 FROM pg_database WHERE datname = '{dbname}'")
    if "1 row" not in check:
        print(db.psql(f"CREATE DATABASE {dbname} OWNER vehicle_admin"), flush=True)
    else:
        print(f"{dbname} already exists", flush=True)

print("READY", flush=True)
print("JDBC vehicle_platform: jdbc:postgresql://localhost:5432/vehicle_platform", flush=True)
print("JDBC vehicle_platform_test: jdbc:postgresql://localhost:5432/vehicle_platform_test", flush=True)

sys.stdout.flush()
while True:
    time.sleep(3600)
