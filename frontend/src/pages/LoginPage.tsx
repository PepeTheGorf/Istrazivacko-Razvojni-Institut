import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { getStoredAuth } from '../auth/authStorage'
import { Button } from '../components/ui/Button'
import { TextInput } from '../components/ui/TextInput'

export function LoginPage() {
  const { login, logout } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)

    try {
      await login({ email, password })
      const stored = getStoredAuth()
      if (stored?.user.role === 'MANAGER') {
        navigate('/projects', { replace: true })
      } else {
        logout()
        setError('Trenutno je podržan pristup samo za menadžere.')
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Prijava nije uspela')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="grid min-h-screen place-items-center bg-canvas p-4 md:p-6">
      <div className="w-full max-w-[440px] rounded-lg border border-hairline bg-surface-1 p-6 md:p-8">
        <header className="mb-6">
          <p className="m-0 mb-2 text-[13px] font-medium tracking-wide text-primary uppercase">
            Istraživačko Razvojni Institut
          </p>
          <h1 className="m-0 text-2xl font-semibold tracking-tight text-ink md:text-[28px]">
            Prijava
          </h1>
          <p className="mt-2 text-sm text-ink-subtle">Prijavite se na svoj nalog</p>
        </header>

        <form className="grid gap-4" onSubmit={(e) => void handleSubmit(e)}>
          <TextInput
            label="Email"
            name="email"
            type="email"
            autoComplete="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <TextInput
            label="Lozinka"
            name="password"
            type="password"
            autoComplete="current-password"
            required
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          {error && (
            <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
              {error}
            </p>
          )}

          <div className="grid gap-3 pt-1">
            <Button type="submit" fullWidth disabled={submitting}>
              {submitting ? 'Prijavljivanje…' : 'Prijavi se'}
            </Button>
            <p className="m-0 text-center text-sm text-ink-subtle">
              Nemate nalog?{' '}
              <Link to="/register" className="text-primary-hover hover:underline">
                Registrujte se
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  )
}
