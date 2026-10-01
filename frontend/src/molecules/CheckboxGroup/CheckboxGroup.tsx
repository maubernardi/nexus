import { useId } from 'react';

import { Checkbox } from '@/components/ui/checkbox';
import { cn } from '@/lib/utils';

type CheckboxOption = { value: string; label: string };

type CheckboxGroupProps = {
  legend: string;
  hint?: string;
  options: readonly CheckboxOption[];
  value: readonly string[];
  onChange: (value: string[]) => void;
  className?: string;
};

/** Scelta multipla come fieldset/legend (il gruppo viene annunciato con il suo titolo). */
export const CheckboxGroup = ({ legend, hint, options, value, onChange, className }: CheckboxGroupProps) => {
  const baseId = useId();
  const hintId = hint ? `${baseId}-hint` : undefined;

  const toggle = (option: string, checked: boolean): void => {
    onChange(checked ? [...value, option] : value.filter((v) => v !== option));
  };

  return (
    <fieldset className={cn('space-y-2', className)} aria-describedby={hintId}>
      <legend className="text-sm font-medium">{legend}</legend>
      {hint && (
        <p id={hintId} className="text-sm text-muted-foreground">
          {hint}
        </p>
      )}
      <div className="grid grid-cols-3 gap-2 sm:grid-cols-4 md:grid-cols-6">
        {options.map((option) => {
          const id = `${baseId}-${option.value}`;
          return (
            <label
              key={option.value}
              htmlFor={id}
              className="flex min-h-11 items-center gap-2 rounded-md border border-border px-3 text-sm"
            >
              <Checkbox
                id={id}
                checked={value.includes(option.value)}
                onChange={(e) => toggle(option.value, e.target.checked)}
              />
              {option.label}
            </label>
          );
        })}
      </div>
    </fieldset>
  );
};
