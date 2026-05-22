import { useEffect, useState } from 'react';
import { NavLink, useLocation } from 'react-router-dom';

export default function AppShell({ children, wide }) {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const onScroll = () => setScrolled(window.scrollY > 8);
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    document.body.style.overflow = menuOpen ? 'hidden' : '';
    return () => { document.body.style.overflow = ''; };
  }, [menuOpen]);

  return (
    <div className="app-shell">
      <header className={`app-header ${scrolled ? 'app-header--scrolled' : ''}`}>
        <div className={`app-header__inner ${wide ? 'app-header__inner--wide' : ''}`}>
          <NavLink to="/" className="brand-link" onClick={() => setMenuOpen(false)}>
            <span className="brand-logo" aria-hidden>☀</span>
            <span>
              <span className="brand-title">Lumenor SolarIQ</span>
              <span className="brand-sub">Energy Analytics</span>
            </span>
          </NavLink>

          <div className="app-header__actions">
            <nav
              className={`app-nav ${menuOpen ? 'app-nav--open' : ''}`}
              aria-label="Main navigation"
            >
              <NavLink
                to="/"
                className={({ isActive }) => `app-nav__link ${isActive ? 'app-nav__link--active' : ''}`}
                end
                onClick={() => setMenuOpen(false)}
              >
                Calculator
              </NavLink>
              <NavLink
                to="/dashboard"
                className={({ isActive }) => `app-nav__link ${isActive ? 'app-nav__link--active' : ''}`}
                onClick={() => setMenuOpen(false)}
              >
                Dashboard
              </NavLink>
            </nav>
            <button
              type="button"
              className="app-nav-toggle"
              aria-expanded={menuOpen}
              aria-label={menuOpen ? 'Close menu' : 'Open menu'}
              onClick={() => setMenuOpen((open) => !open)}
            >
              {menuOpen ? '✕' : '☰'}
            </button>
          </div>
        </div>
      </header>
      <main className={`app-main ${wide ? 'app-main--wide' : ''}`}>{children}</main>
      <footer className="app-footer">
        <div className={`app-footer__inner ${wide ? 'app-footer__inner--wide' : ''}`}>
          © {new Date().getFullYear()} Lumenor · PM Surya Ghar · Enterprise solar intelligence
        </div>
      </footer>
    </div>
  );
}
