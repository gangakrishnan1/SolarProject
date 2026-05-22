export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  disabled = false,
  glow = false,
  fullWidth = false,
  className = '',
  type = 'button',
  ...rest
}) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={[
        'btn-ui',
        `btn-ui--${variant}`,
        `btn-ui--${size}`,
        glow ? 'btn-ui--glow' : '',
        fullWidth ? 'btn-ui--full' : '',
        loading ? 'btn-ui--loading' : '',
        className,
      ].filter(Boolean).join(' ')}
      {...rest}
    >
      {loading && <span className="btn-ui__spinner" aria-hidden />}
      <span className={loading ? 'btn-ui__text btn-ui__text--hidden' : 'btn-ui__text'}>{children}</span>
    </button>
  );
}
