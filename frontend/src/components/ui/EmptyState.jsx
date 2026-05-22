export default function EmptyState({ icon = '🌞', title, description, action, testId }) {
  return (
    <div className="empty-state glass-card" data-testid={testId}>
      <div className="empty-state__icon">{icon}</div>
      <h3 className="empty-state__title">{title}</h3>
      {description && <p className="empty-state__desc">{description}</p>}
      {action}
    </div>
  );
}
