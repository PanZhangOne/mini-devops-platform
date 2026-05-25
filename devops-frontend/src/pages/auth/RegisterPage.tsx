import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Zap } from 'lucide-react'
import { authApi } from '@/api/auth'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'

export function RegisterPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '', nickname: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const set = (key: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm((prev) => ({ ...prev, [key]: e.target.value }))

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await authApi.register({
        username: form.username,
        password: form.password,
        nickname: form.nickname || undefined,
      })
      navigate('/login')
    } catch (err: unknown) {
      const msg =
        (err as Error).message ??
        'Registration failed. Please try again.'
      setError(msg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[var(--color-surface-2)] px-4">
      <div className="w-full max-w-sm">
        <div className="flex items-center justify-center gap-2.5 mb-8">
          <div className="w-9 h-9 rounded-[var(--radius-md)] bg-[var(--color-primary)] flex items-center justify-center">
            <Zap size={18} className="text-white" />
          </div>
          <span className="font-semibold text-lg text-[var(--color-text)]">DevOps Platform</span>
        </div>

        <div className="bg-[var(--color-surface)] rounded-[var(--radius-xl)] shadow-sm border border-[var(--color-border)] p-8">
          <h1 className="text-xl font-semibold text-[var(--color-text)] mb-1">Create account</h1>
          <p className="text-sm text-[var(--color-text-muted)] mb-6">Start using DevOps Platform today</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Username"
              value={form.username}
              onChange={set('username')}
              placeholder="Choose a username"
              required
              autoFocus
            />
            <Input
              label="Nickname"
              value={form.nickname}
              onChange={set('nickname')}
              placeholder="Display name (optional)"
            />
            <Input
              label="Password"
              type="password"
              value={form.password}
              onChange={set('password')}
              placeholder="Create a password"
              required
            />

            {error && (
              <p className="text-sm text-[var(--color-danger)] bg-red-50 px-3 py-2 rounded-[var(--radius-md)]">
                {error}
              </p>
            )}

            <Button type="submit" className="w-full justify-center" loading={loading}>
              Create account
            </Button>
          </form>
        </div>

        <p className="text-center text-sm text-[var(--color-text-muted)] mt-4">
          Already have an account?{' '}
          <Link to="/login" className="text-[var(--color-primary)] hover:underline font-medium">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}
