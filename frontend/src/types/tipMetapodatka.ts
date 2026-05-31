export type TipPodatka = 'TEKST' | 'BROJ' | 'DATUM' | 'BOOLEAN' | 'LISTA'

export interface TipMetapodatka {
  id: string
  naziv: string
  tipPodatka: TipPodatka
  jeObavezan: boolean
  tipDokumentaId?: string | null
}
