package it.nexus.services;

/** Un file da scaricare: nome, tipo e contenuto (US-205). */
public record DownloadedFile(String name, String contentType, byte[] content) {

    @Override
    public String toString() {
        // niente nome del file nei log: può contenere il nome della persona
        return "DownloadedFile[contentType=" + contentType + ", size=" + content.length + "]";
    }
}
