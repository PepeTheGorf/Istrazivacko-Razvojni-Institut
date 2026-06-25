export interface DokumentVerzija {
  id: string
  verzijaBroj: number
  naslov: string
  sadrzajPreview: string
  sacuvaoId: string
  datumKreiranja: string
}

export interface DokumentVerzijaFull extends DokumentVerzija {
  sadrzaj: string
}
