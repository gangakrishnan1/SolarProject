export function formatRupees(amount) {
  if (amount == null || isNaN(Number(amount))) return 'Rs 0';
  const n = Math.round(Number(amount));
  const sign = n < 0 ? '-' : '';
  const abs = Math.abs(n).toString();
  let last3 = abs.slice(-3);
  const rest = abs.slice(0, -3);
  if (rest.length > 0) {
    last3 = ',' + last3;
  }
  const grouped = rest.replace(/\B(?=(\d{2})+(?!\d))/g, ',') + last3;
  return `${sign}Rs ${grouped}`;
}

export function formatKw(kw) {
  if (kw == null || isNaN(Number(kw))) return '0.0 kW';
  return `${Number(kw).toFixed(1)} kW`;
}

export function formatTonnes(kg) {
  if (kg == null || isNaN(Number(kg))) return '0.0 tonnes';
  return `${(Number(kg) / 1000).toFixed(1)} tonnes`;
}

const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export function formatDate(iso) {
  if (!iso) return '';
  const d = new Date(iso);
  if (isNaN(d.getTime())) return '';
  const dd = String(d.getDate()).padStart(2, '0');
  return `${dd} ${MONTHS[d.getMonth()]} ${d.getFullYear()}`;
}
