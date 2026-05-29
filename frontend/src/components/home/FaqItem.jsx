import { useState } from "react";

export default function FaqItem({ q, a }) {
  const [open, setOpen] = useState(false);

  return (
    <div className={`cj-faq-item${open ? " cj-faq-open" : ""}`}>
      <button type="button" className="cj-faq-btn" onClick={() => setOpen((value) => !value)} aria-expanded={open}>
        <span>{q}</span>
        <span className={`cj-faq-icon${open ? " cj-faq-icon-open" : ""}`}>+</span>
      </button>
      <div className="cj-faq-answer" style={{ maxHeight: open ? "200px" : "0" }}>
        <p>{a}</p>
      </div>
    </div>
  );
}
