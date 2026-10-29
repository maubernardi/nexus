#!/usr/bin/env python3
"""Genera i modelli segnaposto dei documenti del tirocinio (US-1004, US-1005) in formato .docx, senza librerie esterne.

Uso: python3 tools/genera-modelli-docx.py   (dalla radice del repository)

Scrive backend/src/main/resources/templates/documents/{convenzione,progetto-formativo}/{CODICE_TIPO,default}.docx. I file sono
riproducibili (date fisse nell'archivio): rigenerarli non cambia nulla se il testo non cambia. Sono modelli provvisori,
da sostituire con i moduli ufficiali della cooperativa (Q5); i segnaposto sono quelli di InternshipDocumentServiceImpl.
"""
import os
import zipfile
from xml.sax.saxutils import escape

OUT = os.path.join(os.path.dirname(__file__), '..', 'backend', 'src', 'main', 'resources', 'templates', 'documents')
FIXED_DATE = (2026, 10, 29, 0, 0, 0)

W = 'http://schemas.openxmlformats.org/wordprocessingml/2006/main'
R = 'http://schemas.openxmlformats.org/officeDocument/2006/relationships'

# tipi di tirocinio iniziali (V21): codice -> come il tipo compare nel titolo; default = tipi aggiunti dall'ADMIN
TYPES = {
    'CURRICULARE': 'CURRICULARE',
    'EXTRACURRICULARE': 'EXTRACURRICULARE',
    'INCLUSIONE': 'DI INCLUSIONE',
    'SOCIO_TERAPEUTICO': 'DI INSERIMENTO SOCIO-TERAPEUTICO',
    'default': '',
}


def title(prefix, suffix):
    return f'{prefix} {suffix}' if suffix else prefix

CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
<Default Extension="xml" ContentType="application/xml"/>
<Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
<Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
<Override PartName="/word/footer1.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.footer+xml"/>
<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>
<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>
</Types>"""

ROOT_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/>
<Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/>
</Relationships>"""

DOCUMENT_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/footer" Target="footer1.xml"/>
</Relationships>"""

STYLES = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="{W}">
<w:docDefaults>
<w:rPrDefault><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri" w:eastAsia="Calibri" w:cs="Calibri"/>
<w:sz w:val="22"/><w:szCs w:val="22"/><w:lang w:val="it-IT"/></w:rPr></w:rPrDefault>
<w:pPrDefault><w:pPr><w:spacing w:after="120" w:line="276" w:lineRule="auto"/></w:pPr></w:pPrDefault>
</w:docDefaults>
<w:style w:type="paragraph" w:default="1" w:styleId="Normal"><w:name w:val="Normal"/><w:qFormat/></w:style>
<w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:basedOn w:val="Normal"/><w:next w:val="Normal"/>
<w:qFormat/><w:pPr><w:jc w:val="center"/><w:spacing w:after="240"/></w:pPr>
<w:rPr><w:b/><w:sz w:val="30"/><w:szCs w:val="30"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="heading 1"/><w:basedOn w:val="Normal"/>
<w:next w:val="Normal"/><w:qFormat/><w:pPr><w:keepNext/><w:spacing w:before="240" w:after="120"/>
<w:outlineLvl w:val="0"/></w:pPr><w:rPr><w:b/><w:sz w:val="24"/><w:szCs w:val="24"/></w:rPr></w:style>
<w:style w:type="paragraph" w:styleId="Nota"><w:name w:val="Nota"/><w:basedOn w:val="Normal"/>
<w:rPr><w:i/><w:color w:val="595959"/><w:sz w:val="18"/><w:szCs w:val="18"/></w:rPr></w:style>
</w:styles>"""

FOOTER = f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:ftr xmlns:w="{W}"><w:p><w:pPr><w:pStyle w:val="Nota"/><w:jc w:val="center"/></w:pPr>
<w:r><w:t xml:space="preserve">NEXUS · segnalazione n. {{NUMERO_SEGNALAZIONE}} · modello segnaposto</w:t></w:r></w:p></w:ftr>"""

APP = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"><Application>NEXUS</Application></Properties>"""


def core(title):
    return f"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
 xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
<dc:title>{escape(title)} (modello segnaposto)</dc:title><dc:creator>NEXUS</dc:creator>
<dcterms:created xsi:type="dcterms:W3CDTF">2026-10-29T00:00:00Z</dcterms:created>
</cp:coreProperties>"""


def para(text, style=None, bold=False):
    """Paragrafo di un solo run, con stile e grassetto facoltativi."""
    ppr = f'<w:pPr><w:pStyle w:val="{style}"/></w:pPr>' if style else ''
    rpr = '<w:rPr><w:b/></w:rPr>' if bold else ''
    return f'<w:p>{ppr}<w:r>{rpr}<w:t xml:space="preserve">{escape(text)}</w:t></w:r></w:p>'


def table(rows):
    """Tabella a due colonne (etichetta in grassetto, valore), bordi semplici."""
    border = '<w:{0} w:val="single" w:sz="4" w:space="0" w:color="808080"/>'
    borders = ''.join(border.format(b) for b in ('top', 'left', 'bottom', 'right', 'insideH', 'insideV'))
    cells = ''
    for label, value in rows:
        cells += ('<w:tr>'
                  f'<w:tc><w:tcPr><w:tcW w:w="3000" w:type="dxa"/></w:tcPr>{para(label, bold=True)}</w:tc>'
                  f'<w:tc><w:tcPr><w:tcW w:w="6638" w:type="dxa"/></w:tcPr>{para(value)}</w:tc>'
                  '</w:tr>')
    return (f'<w:tbl><w:tblPr><w:tblW w:w="5000" w:type="pct"/><w:tblBorders>{borders}</w:tblBorders></w:tblPr>'
            '<w:tblGrid><w:gridCol w:w="3000"/><w:gridCol w:w="6638"/></w:tblGrid>'
            f'{cells}</w:tbl>')


def convenzione(title):
    body = [
        para(title, 'Title'),
        para('Modello segnaposto di NEXUS: il testo definitivo arriverà con i moduli ufficiali della cooperativa.',
             'Nota'),
        para('Tra', 'Heading1'),
        para('Cooperativa Sociale «Il Girasole» – Area Lavoro, soggetto promotore, rappresentata dal tutor '
             '{TUTOR_PROMOTORE} (progetto {PROGETTO});'),
        para('e', 'Heading1'),
        para('{RAGIONE_SOCIALE}, partita IVA {PARTITA_IVA}, con sede legale in {SEDE_LEGALE}, codice ATECO '
             '{CODICE_ATECO}, soggetto ospitante, nella persona del legale rappresentante {LEGALE_RAPPRESENTANTE}, '
             'nato/a a {LUOGO_NASCITA_LEGALE_RAPPRESENTANTE} il {DATA_NASCITA_LEGALE_RAPPRESENTANTE}, codice '
             'fiscale {CODICE_FISCALE_LEGALE_RAPPRESENTANTE}, residente in {RESIDENZA_LEGALE_RAPPRESENTANTE};'),
        para('si conviene quanto segue.'),
        para('Art. 1 – Tirocinante', 'Heading1'),
        para('{NOME_TIROCINANTE}, nato/a a {LUOGO_NASCITA_TIROCINANTE} il {DATA_NASCITA_TIROCINANTE}, codice fiscale '
             '{CODICE_FISCALE_TIROCINANTE}, residente in {RESIDENZA_TIROCINANTE}.'),
        para('Art. 2 – Tirocinio', 'Heading1'),
        table([
            ('Tipo di tirocinio', '{TIPO_TIROCINIO}'),
            ('Mansione', '{MANSIONE}'),
            ('Sede del tirocinio', '{SEDE_TIROCINIO}'),
            ('Periodo', 'dal {DATA_INIZIO} al {DATA_FINE}'),
            ('Periodi di sospensione', '{SOSPENSIONI}'),
        ]),
        para('Art. 3 – Orario e monte ore', 'Heading1'),
        para('{ORE_SETTIMANALI} ore settimanali, per un monte ore complessivo di {MONTE_ORE_TOTALE} ore.'),
        para('Griglia oraria:', bold=True),
        para('{GRIGLIA_ORARIA}'),
        para('Art. 4 – Tutor', 'Heading1'),
        para('Tutor del soggetto ospitante: {TUTOR_AZIENDALI}.'),
        para('Tutor del soggetto promotore: {TUTOR_PROMOTORE}.'),
        para('Firenze, {DATA_DOCUMENTO}'),
        para('Per il soggetto promotore ______________________________'),
        para('Per il soggetto ospitante ______________________________'),
        para('Il tirocinante ______________________________'),
    ]
    sect = (f'<w:sectPr><w:footerReference w:type="default" r:id="rId2"/>'
            '<w:pgSz w:w="11906" w:h="16838"/>'
            '<w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" w:header="567" w:footer="567" '
            'w:gutter="0"/></w:sectPr>')
    return (f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
            f'<w:document xmlns:w="{W}" xmlns:r="{R}"><w:body>{"".join(body)}{sect}</w:body></w:document>')


def progetto_formativo(title):
    lines = '__________________________________________________________________'
    body = [
        para(title, 'Title'),
        para('Modello segnaposto di NEXUS: il testo definitivo arriverà con i moduli ufficiali della cooperativa.',
             'Nota'),
        para('Tirocinante', 'Heading1'),
        para('{NOME_TIROCINANTE}, nato/a a {LUOGO_NASCITA_TIROCINANTE} il {DATA_NASCITA_TIROCINANTE}, codice fiscale '
             '{CODICE_FISCALE_TIROCINANTE}, residente in {RESIDENZA_TIROCINANTE}.'),
        para('Soggetto promotore', 'Heading1'),
        para('Cooperativa Sociale «Il Girasole» – Area Lavoro, progetto {PROGETTO}. Tutor: {TUTOR_PROMOTORE}.'),
        para('Soggetto ospitante', 'Heading1'),
        para('{RAGIONE_SOCIALE}, partita IVA {PARTITA_IVA}, sede legale in {SEDE_LEGALE}, codice ATECO {CODICE_ATECO}. '
             'Legale rappresentante: {LEGALE_RAPPRESENTANTE}. Tutor aziendali: {TUTOR_AZIENDALI}.'),
        para('Tirocinio', 'Heading1'),
        table([
            ('Tipo di tirocinio', '{TIPO_TIROCINIO}'),
            ('Mansione', '{MANSIONE}'),
            ('Sede del tirocinio', '{SEDE_TIROCINIO}'),
            ('Periodo', 'dal {DATA_INIZIO} al {DATA_FINE}'),
            ('Periodi di sospensione', '{SOSPENSIONI}'),
            ('Ore settimanali', '{ORE_SETTIMANALI}'),
            ('Monte ore complessivo', '{MONTE_ORE_TOTALE}'),
        ]),
        para('Articolazione dell\'orario', 'Heading1'),
        para('{GRIGLIA_ORARIA}'),
        para('Obiettivi e attività del tirocinio', 'Heading1'),
        para('Da definire con il tutor aziendale:'),
        para(lines),
        para(lines),
        para(lines),
        para('Obblighi del tirocinante', 'Heading1'),
        para('Il tirocinante svolge le attività previste dal progetto formativo, segue le indicazioni dei tutor, '
             'rispetta le norme su igiene, salute e sicurezza sul lavoro e mantiene la riservatezza sui dati e sulle '
             'informazioni di cui viene a conoscenza.'),
        para('Firenze, {DATA_DOCUMENTO}'),
        para('Il tirocinante ______________________________'),
        para('Il tutor del soggetto promotore ______________________________'),
        para('Il tutor del soggetto ospitante ______________________________'),
    ]
    sect = (f'<w:sectPr><w:footerReference w:type="default" r:id="rId2"/>'
            '<w:pgSz w:w="11906" w:h="16838"/>'
            '<w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134" w:header="567" w:footer="567" '
            'w:gutter="0"/></w:sectPr>')
    return (f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?>\n'
            f'<w:document xmlns:w="{W}" xmlns:r="{R}"><w:body>{"".join(body)}{sect}</w:body></w:document>')


def write(path, title, document):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    parts = [
        ('[Content_Types].xml', CONTENT_TYPES),
        ('_rels/.rels', ROOT_RELS),
        ('docProps/core.xml', core(title)),
        ('docProps/app.xml', APP),
        ('word/document.xml', document),
        ('word/_rels/document.xml.rels', DOCUMENT_RELS),
        ('word/styles.xml', STYLES),
        ('word/footer1.xml', FOOTER),
    ]
    with zipfile.ZipFile(path, 'w', zipfile.ZIP_DEFLATED) as docx:
        for name, content in parts:
            info = zipfile.ZipInfo(name, date_time=FIXED_DATE)
            info.compress_type = zipfile.ZIP_DEFLATED
            docx.writestr(info, content.encode('utf-8'))


def main():
    documents = [
        ('convenzione', 'CONVENZIONE DI TIROCINIO', convenzione),
        ('progetto-formativo', 'PROGETTO FORMATIVO DEL TIROCINIO', progetto_formativo),
    ]
    for folder, prefix, build in documents:
        for code, suffix in TYPES.items():
            path = os.path.normpath(os.path.join(OUT, folder, f'{code}.docx'))
            doc_title = title(prefix, suffix)
            write(path, doc_title, build(doc_title))
            print(path)


if __name__ == '__main__':
    main()
