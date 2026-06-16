export interface SmartDomain {
  id: number
  name: string
}

export interface SmartCategory {
  id: number
  name: string
}

export interface PromptVersion {
  id: number
  content: string
  versionNumber: number
  active: boolean
  createdAt: string
}

export interface TemplateSection {
  id?: number
  title: string
  sectionOrder: number
}

export interface SmartTemplate {
  id: number
  name: string
  description?: string
  domain: SmartDomain
  category: SmartCategory
  sections: TemplateSection[]
  creatorId: number
  createdAt: string
  averageRating?: number
}

export interface TemplateCreationPayload {
  name: string
  description?: string
  domainId?: number    
  newDomain?: string   
  categoryId?: number  
  newCategory?: string 
  sections: Array<{
    title: string
    systemPrompt: string
    order: number
  }>
}

export interface DocumentSection {
  id: number
  title: string;
  templateSectionId: number
  userInput: string      
  llmResult?: string  
  rating?: number           
  feedbackComment?: string   
}

export interface SmartDocument {
  id: number
  templateId: number
  templateName?: string
  researcherId: number
  status: 'DRAFT' | 'COMPLETED'
  createdAt: string
  sections: DocumentSection[]
}
export interface SmartDocumentSummary {
  id: number
  templateName: string
  status: string
  createdAt: string
  progress: number 
}