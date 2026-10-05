const form = document.querySelector("#discount-form");
const unitPriceInput = document.querySelector("#unit-price");
const resultContainer = document.querySelector("#result-container");
const result = document.querySelector("#result");
const copyTotalButton = document.querySelector("#copy-total");
const copyFeedback = document.querySelector("#copy-feedback");

const money = (amount) =>
  new Intl.NumberFormat("es-AR", { style: "currency", currency: "ARS" }).format(Number(amount));

const resetCopyFeedback = () => {
  copyFeedback.hidden = true;
  copyFeedback.className = "copy-feedback";
  copyFeedback.textContent = "";
};

const hideCopyButton = () => {
  copyTotalButton.hidden = true;
  copyTotalButton.dataset.total = "";
  resetCopyFeedback();
};

const resetCalculationUI = () => {
  resultContainer.hidden = true;
  result.className = "result";
  result.textContent = "";
  hideCopyButton();
};

const setCopyFeedback = (type, message) => {
  copyFeedback.className = `copy-feedback ${type}`;
  copyFeedback.textContent = message;
  copyFeedback.hidden = false;
};

copyTotalButton.addEventListener("click", async () => {
  const total = copyTotalButton.dataset.total;

  if (!total) {
    setCopyFeedback("error", "No hay un total listo para copiar.");
    return;
  }

  if (!navigator.clipboard?.writeText) {
    setCopyFeedback("error", "Tu navegador no permite copiar automáticamente el total.");
    return;
  }

  try {
    await navigator.clipboard.writeText(total);
    setCopyFeedback("success", "Total copiado al portapapeles.");
  } catch {
    setCopyFeedback("error", "No se pudo copiar el total. Probá de nuevo.");
  }
});

form.addEventListener("reset", () => {
  resetCalculationUI();
  unitPriceInput.focus();
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  resetCalculationUI();

  const formData = new FormData(form);
  const payload = {
    unitPrice: formData.get("unitPrice"),
    quantity: Number(formData.get("quantity")),
    couponCode: formData.get("couponCode") || null,
  };

  try {
    const response = await fetch("/api/discounts/calculate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
    const body = await response.json();

    if (!response.ok) {
      throw new Error(body.error);
    }

    const formattedTotal = money(body.total);
    result.className = "result success";
    result.innerHTML = `
      <strong>Total final: ${formattedTotal}</strong>
      <dl>
        <div><dt>Subtotal</dt><dd>${money(body.subtotal)}</dd></div>
        <div><dt>Descuento (${body.discountPercentage}%)</dt><dd>-${money(body.discountAmount)}</dd></div>
      </dl>`;
    copyTotalButton.dataset.total = formattedTotal;
    copyTotalButton.hidden = false;
  } catch (error) {
    result.className = "result error";
    result.textContent = error.message || "No se pudo calcular el descuento.";
  }

  resultContainer.hidden = false;
});
