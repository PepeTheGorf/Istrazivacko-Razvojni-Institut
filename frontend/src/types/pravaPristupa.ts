export interface PravaPristupa {
  id: string
  korisnikId: string
  dokumentId?: string
  projekatId?: string
  nivo: 'CITANJE' | 'IZMENA' | 'ZABRANA'
  dodeljivaoId: string
  datumDodele: string
}

export type NivoPrava = 'CITANJE' | 'IZMENA' | 'ZABRANA'
