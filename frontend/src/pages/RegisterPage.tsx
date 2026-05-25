import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { Button } from '../components/ui/Button'
import { SelectField } from '../components/ui/SelectField'
import { TextInput } from '../components/ui/TextInput'
import type { RegisterableRole } from '../types/auth'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [surname, setSurname] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [role, setRole] = useState<RegisterableRole>('MANAGER')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)

    if (password !== confirmPassword) {
      setError('Lozinke se ne poklapaju')
      return
    }

    setSubmitting(true)

    try {
      await register({ name, surname, email, password, role })
      if (role === 'MANAGER') {
        navigate('/projects', { replace: true })
      } else {
        navigate('/login', { replace: true })
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registracija nije uspela')
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
            Registracija
          </h1>
          <p className="mt-2 text-sm text-ink-subtle">
            Kreirajte nalog menadžera ili člana tima
          </p>
        </header>

        <form className="grid gap-4" onSubmit={(e) => void handleSubmit(e)}>
          <div className="grid gap-4 md:grid-cols-2">
            <TextInput
              label="Ime"
              name="name"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <TextInput
              label="Prezime"
              name="surname"
              required
              value={surname}
              onChange={(e) => setSurname(e.target.value)}
            />
          </div>

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
            autoComplete="new-password"
            required
            minLength={8}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <TextInput
            label="Potvrda lozinke"
            name="confirmPassword"
            type="password"
            autoComplete="new-password"
            required
            minLength={8}
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />

          <SelectField
            label="Uloga"
            name="role"
            value={role}
            onChange={(e) => setRole(e.target.value as RegisterableRole)}
          >
            <option value="MANAGER">Menadžer</option>
            <option value="TEAM_MEMBER">Član tima</option>
          </SelectField>

          {error && (
            <p className="m-0 rounded-md border border-error/35 bg-error/10 px-3 py-3 text-sm text-[#ffb4b4]">
              {error}
            </p>
          )}

          <div className="grid gap-3 pt-1">
            <Button type="submit" fullWidth disabled={submitting}>
              {submitting ? 'Registracija…' : 'Registruj se'}
            </Button>
            <p className="m-0 text-center text-sm text-ink-subtle">
              Već imate nalog?{' '}
              <Link to="/login" className="text-primary-hover hover:underline">
                Prijavite se
              </Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  )
}
