import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ArrowRight, BarChart3, Car, GitCompare, ScanLine, Search, ShieldCheck,
  Database, Gauge, Image as ImageIcon,
} from 'lucide-react'

const features = [
  {
    icon: Search,
    title: 'Vehicle Search',
    description: 'Recherchez par marque, modèle, année, motorisation ou carburant avec des filtres combinables.',
    to: '/search',
  },
  {
    icon: ScanLine,
    title: 'VIN Decoder',
    description: 'Décodage VIN en temps réel via l\'API officielle NHTSA — pays, usine, moteur, série.',
    to: '/vin-decoder',
  },
  {
    icon: Car,
    title: 'Vehicle Analysis',
    description: 'Fiches techniques détaillées : motorisation, performances, dimensions, consommation, sécurité.',
    to: '/search',
  },
  {
    icon: GitCompare,
    title: 'Vehicle Comparison',
    description: 'Comparez jusqu\'à 4 véhicules côte à côte avec mise en évidence automatique des meilleures valeurs.',
    to: '/compare',
  },
  {
    icon: BarChart3,
    title: 'Data Dashboard',
    description: 'Statistiques de vos recherches, décodages VIN et tendances par graphiques interactifs.',
    to: '/dashboard',
  },
]

const steps = [
  { icon: Search, label: 'Search' },
  { icon: Database, label: 'Retrieve Data' },
  { icon: ScanLine, label: 'Decode' },
  { icon: Gauge, label: 'Analyze' },
  { icon: GitCompare, label: 'Compare' },
]

export function HomePage() {
  const [query, setQuery] = useState('')
  const navigate = useNavigate()

  function handleSearch(e: FormEvent) {
    e.preventDefault()
    navigate(query ? `/search?q=${encodeURIComponent(query)}` : '/search')
  }

  return (
    <div>
      {/* HERO */}
      <section className="relative overflow-hidden bg-grid border-b border-[var(--color-border)]">
        <div className="bg-radial-glow absolute inset-0 pointer-events-none" />
        <div className="relative max-w-5xl mx-auto px-4 sm:px-6 pt-20 pb-24 text-center">
          <h1 className="font-display text-4xl sm:text-6xl font-bold text-[var(--color-text)] leading-tight">
            Explore. Decode. <span className="text-gradient">Analyze.</span> Compare.
          </h1>
          <p className="mt-5 text-base sm:text-lg text-[var(--color-text-muted)] max-w-2xl mx-auto">
            Une plateforme unique pour rechercher, identifier et comparer des véhicules à partir
            de sources automobiles fiables — décodage VIN réel, fiches techniques détaillées et
            comparaison intelligente.
          </p>

          <form onSubmit={handleSearch} className="mt-8 max-w-xl mx-auto flex flex-col sm:flex-row gap-2">
            <input
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="BMW Série 3, Tesla Model 3, Toyota Corolla..."
              className="input-field flex-1 px-4 py-3 text-sm"
            />
            <button type="submit" className="btn-primary px-5 py-3 text-sm flex items-center gap-2 shrink-0">
              <Search size={16} /> Rechercher
            </button>
          </form>

          <div className="mt-4 flex flex-wrap items-center justify-center gap-3">
            <Link to="/vin-decoder" className="btn-secondary px-4 py-2 text-sm flex items-center gap-2">
              <ScanLine size={15} /> VIN Decoder
            </Link>
            <Link to="/compare" className="btn-secondary px-4 py-2 text-sm flex items-center gap-2">
              <GitCompare size={15} /> Comparer des véhicules
            </Link>
          </div>
        </div>
      </section>

      {/* FEATURES */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 py-20">
        <div className="text-center mb-12">
          <h2 className="font-display text-2xl sm:text-3xl font-bold text-[var(--color-text)]">
            Tout ce dont vous avez besoin pour analyser un véhicule
          </h2>
          <p className="text-[var(--color-text-muted)] mt-2">Cinq modules complémentaires, une seule plateforme.</p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          {features.map((f) => (
            <Link key={f.title} to={f.to} className="card-elevated card-hover p-6 group">
              <div className="h-11 w-11 rounded-xl bg-[var(--color-primary-500)]/10 flex items-center justify-center text-[var(--color-primary-400)] mb-4">
                <f.icon size={20} />
              </div>
              <h3 className="font-display font-semibold text-[var(--color-text)] flex items-center gap-1.5">
                {f.title}
                <ArrowRight size={14} className="opacity-0 group-hover:opacity-100 transition-opacity" />
              </h3>
              <p className="text-sm text-[var(--color-text-muted)] mt-2 leading-relaxed">{f.description}</p>
            </Link>
          ))}
        </div>
      </section>

      {/* PROCESS */}
      <section className="border-y border-[var(--color-border)] bg-[var(--color-surface)]/40">
        <div className="max-w-5xl mx-auto px-4 sm:px-6 py-20">
          <div className="text-center mb-12">
            <h2 className="font-display text-2xl sm:text-3xl font-bold text-[var(--color-text)]">Comment ça fonctionne</h2>
          </div>
          <div className="flex flex-col sm:flex-row items-center justify-between gap-6">
            {steps.map((s, i) => (
              <div key={s.label} className="flex items-center gap-4 sm:flex-col sm:gap-3">
                <div className="h-14 w-14 rounded-2xl bg-[var(--color-surface-2)] border border-[var(--color-border)] flex items-center justify-center text-[var(--color-primary-400)]">
                  <s.icon size={22} />
                </div>
                <span className="font-display font-semibold text-sm text-[var(--color-text)]">{s.label}</span>
                {i < steps.length - 1 && (
                  <ArrowRight size={18} className="hidden sm:block text-[var(--color-text-faint)] mt-[-24px]" />
                )}
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* SOURCES */}
      <section className="max-w-5xl mx-auto px-4 sm:px-6 py-20">
        <div className="text-center mb-10">
          <h2 className="font-display text-2xl sm:text-3xl font-bold text-[var(--color-text)]">Sources de données</h2>
          <p className="text-[var(--color-text-muted)] mt-2">Traçabilité complète — jamais de donnée inventée.</p>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div className="card-elevated p-6">
            <div className="flex items-center gap-2 mb-2">
              <ShieldCheck size={18} className="text-[var(--color-success-400)]" />
              <h3 className="font-display font-semibold text-[var(--color-text)]">NHTSA vPIC</h3>
              <span className="badge badge-live ml-auto">Live API</span>
            </div>
            <p className="text-sm text-[var(--color-text-muted)]">
              API publique et gratuite du gouvernement américain (NHTSA), utilisée pour le décodage
              VIN réel et les listes marque/modèle.
            </p>
          </div>
          <div className="card-elevated p-6">
            <div className="flex items-center gap-2 mb-2">
              <Database size={18} className="text-[var(--color-amber-400)]" />
              <h3 className="font-display font-semibold text-[var(--color-text)]">Jeu de données local</h3>
              <span className="badge badge-demo ml-auto">Demo data</span>
            </div>
            <p className="text-sm text-[var(--color-text-muted)]">
              Spécifications techniques complètes (puissance, consommation, CO₂...) issues d'un
              jeu de données curé localement, clairement identifié comme donnée de démonstration.
            </p>
          </div>
          <div className="card-elevated p-6">
            <div className="flex items-center gap-2 mb-2">
              <ImageIcon size={18} className="text-[var(--color-success-400)]" />
              <h3 className="font-display font-semibold text-[var(--color-text)]">CarsXE</h3>
              <span className="badge badge-live ml-auto">Live API</span>
            </div>
            <p className="text-sm text-[var(--color-text-muted)]">
              Photos réelles des véhicules récupérées une seule fois par modèle puis conservées
              en base — aucune image inventée ou générique.
            </p>
          </div>
          <div className="card-elevated p-6">
            <div className="flex items-center gap-2 mb-2">
              <Search size={18} className="text-[var(--color-success-400)]" />
              <h3 className="font-display font-semibold text-[var(--color-text)]">API Ninjas</h3>
              <span className="badge badge-live ml-auto">Live API</span>
            </div>
            <p className="text-sm text-[var(--color-text-muted)]">
              Recherche en direct utilisée en secours pour les véhicules absents du catalogue
              local (marque, modèle, motorisation, transmission).
            </p>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="max-w-5xl mx-auto px-4 sm:px-6 pb-20">
        <div className="card-elevated p-10 text-center bg-radial-glow">
          <h2 className="font-display text-2xl font-bold text-[var(--color-text)]">Prêt à explorer ?</h2>
          <p className="text-[var(--color-text-muted)] mt-2 mb-6">Commencez une recherche ou décodez un VIN dès maintenant.</p>
          <div className="flex flex-wrap justify-center gap-3">
            <Link to="/search" className="btn-primary px-6 py-3 text-sm">Commencer une recherche</Link>
            <Link to="/compare" className="btn-secondary px-6 py-3 text-sm">Comparer des véhicules</Link>
          </div>
        </div>
      </section>
    </div>
  )
}
