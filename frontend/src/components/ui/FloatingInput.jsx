export default function FloatingInput({
  id,
  label,
  type = 'text',
  value,
  onChange,
  icon,
  error,
  required,
  placeholder = ' ',
  className = '',
  as = 'input',
  rows,
  style,
  ...rest
}) {
  const Tag = as;
  const hasValue = value != null && String(value).length > 0;

  return (
    <div className={`float-field ${hasValue ? 'float-field--filled' : ''} ${error && error !== ' ' ? 'float-field--error' : ''} ${className}`}>
      <div className="float-field__wrap">
        {icon && <span className="float-field__icon" aria-hidden>{icon}</span>}
        <Tag
          id={id}
          className={`float-field__input ${icon ? 'float-field__input--icon' : ''} ${as === 'textarea' ? 'float-field__input--textarea' : ''}`}
          type={as === 'input' ? type : undefined}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          rows={as === 'textarea' ? rows : undefined}
          style={style}
          {...rest}
        />
        <label
          htmlFor={id}
          className={`float-field__label ${hasValue ? 'float-field__label--float' : ''}`}
        >
          {label}
        </label>
      </div>
      {error && error !== ' ' && <span className="float-field__error">{error}</span>}
    </div>
  );
}
