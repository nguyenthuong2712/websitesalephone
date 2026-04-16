export const formatCurrency = (value: number | string) => {
    if (!value) return "0₫";
    return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
    }).format(Number(value));
};
export const getContrastColor = (hex: string): string => {
    if (!hex) return "#000";

    // Gọt bỏ dấu #
    const c = hex.replace("#", "");

    // Parse RGB
    const r = parseInt(c.slice(0, 2), 16);
    const g = parseInt(c.slice(2, 4), 16);
    const b = parseInt(c.slice(4, 6), 16);

    const brightness = (r * 299 + g * 587 + b * 114) / 1000;

    return brightness > 125 ? "#000" : "#fff";
};
