export function isBlank(value: string | null | undefined) {
  return !(value ?? '').trim()
}

export function isValidDateInput(value: string | null | undefined) {
  const raw = (value ?? '').trim()
  if (!raw) return false
  if (!/^\d{4}-\d{2}-\d{2}$/.test(raw)) return false
  const parsed = new Date(`${raw}T00:00:00`)
  return !Number.isNaN(parsed.getTime())
}

export function isValidDateTimeLocal(value: string | null | undefined) {
  const raw = (value ?? '').trim()
  if (!raw) return false
  return !Number.isNaN(new Date(raw).getTime())
}

function startOfDayMs(value: string) {
  return new Date(`${value}T00:00:00`).getTime()
}

export function isFutureDate(value: string | null | undefined) {
  const raw = (value ?? '').trim()
  if (!isValidDateInput(raw)) return false

  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  return startOfDayMs(raw) > today
}

export function isFutureDateTime(value: string | null | undefined) {
  const raw = (value ?? '').trim()
  if (!isValidDateTimeLocal(raw)) return false
  return new Date(raw).getTime() > Date.now()
}

export function toFriendlyFormError(error: unknown, fallbackAction: string) {
  const raw = String(error ?? '').trim()

  if (!raw) return `${fallbackAction} could not be completed. Please try again.`
  if (raw.includes('401')) return 'Your session is not authorized for this action. Please sign in again.'
  if (raw.includes('403')) return 'You do not have permission to do that.'
  if (raw.includes('404')) return 'The requested record could not be found.'
  if (raw.includes('409')) return 'This record could not be saved because it conflicts with existing data.'
  if (raw.includes('422')) return 'Some values were not accepted. Please review the form and try again.'
  if (raw.includes('500')) return 'The server could not complete this request. Please try again.'

  return `${fallbackAction} could not be completed. ${raw}`
}

export function invalidFieldClass(hasError: boolean) {
  return hasError ? 'border-red-300 focus-visible:ring-red-200' : ''
}
