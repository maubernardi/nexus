/**
 * Salva un file ricevuto da un'API autenticata: un semplice link non porterebbe il token, quindi il file arriva come
 * blob e si scarica con un link temporaneo.
 */
export const saveBlob = (blob: Blob, filename: string): void => {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
};
