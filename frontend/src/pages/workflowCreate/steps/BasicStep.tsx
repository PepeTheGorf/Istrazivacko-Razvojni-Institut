import { TextArea } from '../../../components/ui/TextArea'
import { TextInput } from '../../../components/ui/TextInput'

interface BasicStepProps {
  name: string
  description: string
  onNameChange: (value: string) => void
  onDescriptionChange: (value: string) => void
}

export function BasicStep({ name, description, onNameChange, onDescriptionChange }: BasicStepProps) {
  return (
    <div className="grid w-full gap-6 py-2">
      <TextInput
        label="Naziv Toka Rada"
        name="workflow-name"
        required
        value={name}
        onChange={(e) => onNameChange(e.target.value)}
        placeholder="Upiši naziv toka rada.."
      />
      <TextArea
        label="Opis Toka Rada"
        name="workflow-description"
        value={description}
        onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) =>
          onDescriptionChange(e.target.value)
        }
        placeholder="Upiši opis toka rada..."
      />
    </div>
  )
}
