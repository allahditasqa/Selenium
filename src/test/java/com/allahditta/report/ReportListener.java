package com.allahditta.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that writes module-wise reports of the REAL run:
 * <pre>
 * reports/index.html, reports/history.json
 * reports/&lt;module-slug&gt;/&lt;Module&gt; test &lt;DD-MM-YYYY&gt;_&lt;HH-mm-ss&gt;.html   (self-contained)
 * reports/&lt;module-slug&gt;/&lt;Module&gt; test cases &lt;DD-MM-YYYY&gt;_&lt;HH-mm-ss&gt;.csv
 * </pre>
 * Test case details (title, steps, expected result ...) come from the master CSVs in docs/test-cases.
 * A test is linked to its case by the ID in {@code @Test(description = "<case id> - <title>")} or by a data
 * provider whose first parameter is the ID.
 * System properties: report.title, report.env, report.dir (default "reports"), docs.dir.
 */
public class ReportListener implements ITestListener, ISuiteListener {

    /** Optional: a test class can register a supplier that returns a data URI screenshot of its browser. */
    public static final ThreadLocal<Supplier<String>> SCREENSHOT = new ThreadLocal<>();

    private static final Pattern ID = Pattern.compile("TC_[A-Z0-9]+(?:_[A-Z0-9]+)*_\\d{2,3}");
    private static final DateTimeFormatter FILE_STAMP = DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss", Locale.ROOT);
    private static final DateTimeFormatter HUMAN = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ROOT);

    private static final class Result {
        String id; String status; double seconds; String actual; String screenshot;
    }

    private final Map<String, Result> results = new ConcurrentHashMap<>();
    private volatile long suiteStart;

    // ------------------------------------------------------------------ TestNG callbacks
    @Override public void onStart(ISuite suite) { suiteStart = System.currentTimeMillis(); }

    @Override public void onTestSuccess(ITestResult r) { record(r, "passed", "As expected (all assertions passed)"); }

    @Override public void onTestFailure(ITestResult r) { record(r, "failed", describe(r.getThrowable())); }

    @Override public void onTestSkipped(ITestResult r) {
        record(r, "skipped", r.getThrowable() != null ? describe(r.getThrowable()) : "Skipped");
    }

    private void record(ITestResult r, String status, String actual) {
        String id = idOf(r);
        if (id == null) return;
        Result res = new Result();
        res.id = id;
        res.status = status;
        res.seconds = (r.getEndMillis() - r.getStartMillis()) / 1000.0;
        res.actual = actual;
        Supplier<String> shot = SCREENSHOT.get();
        if (shot != null) {
            try { res.screenshot = shot.get(); } catch (Exception ignored) { /* browser may be gone */ }
        }
        results.put(id, res);
    }

    private static String idOf(ITestResult r) {
        Object[] params = r.getParameters();
        if (params != null && params.length > 0 && params[0] instanceof String s && ID.matcher(s).matches()) return s;
        String d = r.getMethod().getDescription();
        if (d != null) { Matcher m = ID.matcher(d); if (m.find()) return m.group(); }
        return null;
    }

    private static String describe(Throwable t) {
        if (t == null) return "Failed";
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String[] lines = sw.toString().split("\\R");
        return Stream.of(lines).limit(8).collect(Collectors.joining("\n"));
    }

    @Override public void onFinish(ISuite suite) {
        if (results.isEmpty()) return;
        try {
            write(Instant.ofEpochMilli(suiteStart), (System.currentTimeMillis() - suiteStart) / 1000.0);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write reports", e);
        }
    }

    // ------------------------------------------------------------------ docs
    private record Doc(String id, String module, String title, String priority, String type, String pre,
                       String data, String steps, String expected, String testFile) { }

    private static List<Doc> loadDocs() throws IOException {
        Path dir = Path.of(System.getProperty("docs.dir", "docs/test-cases"));
        List<Doc> docs = new ArrayList<>();
        if (!Files.isDirectory(dir)) return docs;
        List<Path> files;
        try (Stream<Path> s = Files.list(dir)) { files = s.filter(p -> p.toString().endsWith(".csv")).sorted().toList(); }
        for (Path f : files) {
            List<List<String>> rows = parseCsv(Files.readString(f, StandardCharsets.UTF_8));
            if (rows.isEmpty()) continue;
            List<String> head = rows.get(0);
            for (List<String> row : rows.subList(1, rows.size())) {
                Map<String, String> m = new LinkedHashMap<>();
                for (int i = 0; i < head.size() && i < row.size(); i++) m.put(head.get(i), row.get(i));
                docs.add(new Doc(m.get("ID"), m.get("Module"), m.get("Title"), m.get("Priority"), m.get("Type"),
                        m.get("Preconditions"), m.get("Test Data"), m.get("Steps"), m.get("Expected Result"), m.get("Test File")));
            }
        }
        return docs;
    }

    static List<List<String>> parseCsv(String text) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        if (text.startsWith("\uFEFF")) text = text.substring(1);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < text.length() && text.charAt(i + 1) == '"') { cur.append('"'); i++; }
                else if (c == '"') quoted = false;
                else cur.append(c);
            } else if (c == '"') quoted = true;
            else if (c == ',') { row.add(cur.toString()); cur.setLength(0); }
            else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') i++;
                row.add(cur.toString()); cur.setLength(0);
                if (row.size() > 1 || !row.get(0).isEmpty()) rows.add(row);
                row = new ArrayList<>();
            } else cur.append(c);
        }
        if (cur.length() > 0 || !row.isEmpty()) { row.add(cur.toString()); rows.add(row); }
        return rows;
    }

    // ------------------------------------------------------------------ writing
    private void write(Instant startedInstant, double durationSeconds) throws IOException {
        Path reports = Path.of(System.getProperty("report.dir", "reports"));
        String title = System.getProperty("report.title", "Tests");
        String env = System.getProperty("report.env", "");
        ZonedDateTime started = startedInstant.atZone(ZoneId.systemDefault());
        String stamp = FILE_STAMP.format(started);

        Map<String, List<Doc>> byModule = new LinkedHashMap<>();
        for (Doc d : loadDocs()) byModule.computeIfAbsent(d.module(), k -> new ArrayList<>()).add(d);
        byModule.values().forEach(l -> l.sort(java.util.Comparator.comparing(Doc::id)));
        // results whose ID is not documented are still reported (the traceability check forbids this)
        for (Result r : results.values()) {
            boolean known = byModule.values().stream().flatMap(List::stream).anyMatch(d -> d.id().equals(r.id));
            if (!known) byModule.computeIfAbsent("Undocumented", k -> new ArrayList<>())
                    .add(new Doc(r.id, "Undocumented", r.id, "", "", "", "", "", "", ""));
        }

        StringBuilder entry = new StringBuilder();
        entry.append("  {\n    \"RunId\": \"").append(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.ROOT).format(started)).append("\",\n")
                .append("    \"StartedAt\": \"").append(startedInstant).append("\",\n")
                .append("    \"DurationSeconds\": ").append(String.format(Locale.ROOT, "%.1f", durationSeconds)).append(",\n")
                .append("    \"Modules\": [\n");
        List<String> moduleJson = new ArrayList<>();

        for (Map.Entry<String, List<Doc>> m : byModule.entrySet()) {
            List<Doc> docs = m.getValue();
            if (docs.stream().noneMatch(d -> results.containsKey(d.id()))) continue; // module not run at all
            String name = m.getKey();
            String slug = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            Path dir = reports.resolve(slug);
            Files.createDirectories(dir);
            String htmlName = name + " test " + stamp + ".html";
            String csvName = name + " test cases " + stamp + ".csv";

            int passed = 0, failed = 0, skipped = 0;
            double seconds = 0;
            StringBuilder cards = new StringBuilder();
            StringBuilder csv = new StringBuilder("\uFEFF");
            csv.append(Stream.of("Test Case ID", "Module", "Test Case Title", "Priority", "Test Type", "Preconditions", "Test Steps",
                    "Test Data", "Expected Result", "Actual Result", "Execution Status", "Execution Type", "Automation Method",
                    "Duration (s)", "Executed On", "Environment").map(ReportListener::q).collect(Collectors.joining(","))).append("\r\n");
            for (Doc d : docs) {
                Result r = results.get(d.id());
                String status = r == null ? "skipped" : r.status;
                String actual = r == null ? "Not executed in this run" : r.actual;
                double secs = r == null ? 0 : r.seconds;
                seconds += secs;
                switch (status) { case "passed" -> passed++; case "failed" -> failed++; default -> skipped++; }
                cards.append(card(d, status, actual, secs, r == null ? null : r.screenshot));
                String label = switch (status) { case "passed" -> "Pass"; case "failed" -> "Fail"; default -> "Skip"; };
                csv.append(Stream.of(d.id(), name, d.title(), d.priority(), d.type(), d.pre(), d.steps(), d.data(), d.expected(),
                        actual.replaceAll("\\s*\\R\\s*", " | "), label, "Automated", d.testFile(),
                        String.format(Locale.ROOT, "%.1f", secs), HUMAN.format(started), env).map(ReportListener::q).collect(Collectors.joining(",")))
                        .append("\r\n");
            }
            String html = page(esc(name) + " Test Report " + HUMAN.format(started), "<p class=\"meta\"><a href=\"../index.html\">&larr; All reports</a></p><h1>" + esc(name) + "</h1>"
                    + "<div class=\"meta\">Run: " + HUMAN.format(started) + " &middot; duration " + Math.round(seconds) + "s &middot; overall: " + (failed > 0 ? "failed" : "passed")
                    + " &middot; " + esc(env) + " &middot; click a screenshot to enlarge</div>"
                    + summary(docs.size(), passed, failed, skipped) + cards);
            Files.writeString(dir.resolve(htmlName), html, StandardCharsets.UTF_8);
            Files.writeString(dir.resolve(csvName), csv.toString(), StandardCharsets.UTF_8);

            moduleJson.add("      {\n        \"Module\": \"" + json(name) + "\",\n        \"ReportPath\": \"" + json(slug + "/" + htmlName) + "\",\n"
                    + "        \"CsvPath\": \"" + json(slug + "/" + csvName) + "\",\n        \"Total\": " + docs.size() + ",\n        \"Passed\": " + passed + ",\n"
                    + "        \"Failed\": " + failed + ",\n        \"Skipped\": " + skipped + ",\n        \"DurationSeconds\": " + String.format(Locale.ROOT, "%.1f", seconds) + "\n      }");
        }
        entry.append(String.join(",\n", moduleJson)).append("\n    ]\n  }");

        Path historyFile = reports.resolve("history.json");
        List<String> entries = new ArrayList<>();
        entries.add(entry.toString());
        if (Files.exists(historyFile)) entries.addAll(splitObjects(Files.readString(historyFile, StandardCharsets.UTF_8)));
        Files.writeString(historyFile, "[\n" + String.join(",\n", entries) + "\n]", StandardCharsets.UTF_8);
        Files.writeString(reports.resolve("index.html"), index(title, entries), StandardCharsets.UTF_8);
        System.out.println("Reports written to " + reports.toAbsolutePath().resolve("index.html"));
    }

    private static List<String> splitObjects(String text) {
        List<String> out = new ArrayList<>();
        int depth = 0, start = -1;
        boolean inString = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (inString) { if (c == '\\') i++; else if (c == '"') inString = false; continue; }
            if (c == '"') inString = true;
            else if (c == '{') { if (depth++ == 0) start = i; }
            else if (c == '}' && --depth == 0) out.add("  " + text.substring(start, i + 1).replaceAll("^\\s+", ""));
        }
        return out;
    }

    // ------------------------------------------------------------------ index
    private static String index(String title, List<String> entries) {
        StringBuilder sb = new StringBuilder();
        for (String e : entries) {
            String start = field(e, "StartedAt");
            ZonedDateTime d = OffsetDateTime.parse(start).atZoneSameInstant(ZoneId.systemDefault());
            double dur = Double.parseDouble(field(e, "DurationSeconds"));
            Matcher mods = Pattern.compile("\\{[^{}]*\"Module\"[^{}]*\\}").matcher(e);
            StringBuilder rows = new StringBuilder();
            int t = 0, p = 0, f = 0, s = 0;
            while (mods.find()) {
                String m = mods.group();
                int total = Integer.parseInt(field(m, "Total")), pa = Integer.parseInt(field(m, "Passed")),
                        fa = Integer.parseInt(field(m, "Failed")), sk = Integer.parseInt(field(m, "Skipped"));
                t += total; p += pa; f += fa; s += sk;
                rows.append("<tr><td>").append(esc(unjson(field(m, "Module")))).append("</td><td>").append(total).append("</td><td class=\"passed-t\">").append(pa)
                        .append("</td><td class=\"").append(fa > 0 ? "failed-t" : "").append("\">").append(fa).append("</td><td class=\"skipped-t\">").append(sk)
                        .append("</td><td class=\"").append(fa > 0 ? "failed-t\">Failed" : "passed-t\">Passed").append("</td><td><a href=\"")
                        .append(uri(unjson(field(m, "ReportPath")))).append("\">HTML</a></td><td><a href=\"").append(uri(unjson(field(m, "CsvPath")))).append("\">CSV</a></td></tr>");
            }
            sb.append("<h2>").append(DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ROOT).format(d)).append(" &nbsp;&middot;&nbsp; ")
                    .append(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT).format(d)).append(" <span class=\"meta\">&nbsp;total ").append(t)
                    .append(" &middot; <span class=\"passed-t\">").append(p).append(" pass</span> &middot; <span class=\"").append(f > 0 ? "failed-t" : "").append("\">")
                    .append(f).append(" fail</span> &middot; <span class=\"skipped-t\">").append(s).append(" skip</span> &middot; ").append(Math.round(dur)).append("s</span></h2>")
                    .append("<table><thead><tr><th>Module</th><th>Total</th><th>Pass</th><th>Fail</th><th>Skip</th><th>Result</th><th>Report</th><th>Test cases</th></tr></thead><tbody>")
                    .append(rows).append("</tbody></table>");
        }
        return page(esc(title) + " - Test Reports", "<h1>" + esc(title) + " - Test Reports</h1><div class=\"meta\">History: newest run on top. Under each date-time you see the module-wise reports. Old runs are never overwritten or deleted.</div>" + sb);
    }

    private static String field(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(\"((?:[^\"\\\\]|\\\\.)*)\"|[-0-9.]+)").matcher(json);
        if (!m.find()) throw new IllegalStateException("history.json: missing " + key);
        return m.group(2) != null ? m.group(2) : m.group(1);
    }

    // ------------------------------------------------------------------ html helpers
    private static String card(Doc d, String status, String actual, double secs, String screenshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("<section class=\"card ").append(status).append("\"><div class=\"head\"><span class=\"badge ").append(status).append("\">").append(status)
                .append("</span><b>").append(esc(d.id())).append(" - ").append(esc(d.title())).append("</b><span class=\"meta\">").append(esc(d.testFile()))
                .append(" &middot; ").append(String.format(Locale.ROOT, "%.1f", secs)).append("s</span></div><div class=\"meta\">").append(esc(d.priority()))
                .append(" &middot; ").append(esc(d.type())).append("</div><details><summary class=\"meta\">Test case details</summary><table><tr><th>Preconditions</th><td>")
                .append(esc(d.pre())).append("</td></tr><tr><th>Test steps</th><td>").append(esc(d.steps()).replaceAll("\\s(?=\\d+\\.\\s)", "<br>"))
                .append("</td></tr><tr><th>Test data</th><td>").append(esc(d.data())).append("</td></tr><tr><th>Expected result</th><td>").append(esc(d.expected()))
                .append("</td></tr></table></details><div class=\"meta\">Actual result:</div><pre").append(status.equals("failed") ? " class=\"err\"" : "").append(">")
                .append(esc(actual)).append("</pre>");
        if (screenshot != null) sb.append("<div class=\"shots\"><figure><img src=\"").append(screenshot).append("\" alt=\"screenshot\" onclick=\"this.classList.toggle('big')\"><figcaption>screenshot at the end of the test</figcaption></figure></div>");
        return sb.append("</section>").toString();
    }

    private static String summary(int total, int p, int f, int s) {
        return "<div class=\"sum\"><div><b>" + total + "</b>total</div><div><b class=\"passed-t\">" + p + "</b>passed</div><div><b class=\"" + (f > 0 ? "failed-t" : "")
                + "\">" + f + "</b>failed</div><div><b class=\"skipped-t\">" + s + "</b>skipped</div></div>";
    }

    private static String page(String title, String body) {
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"><title>" + title + "</title><style>" + CSS
                + "</style></head><body><main>" + body + "</main></body></html>";
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String q(String s) { return "\"" + (s == null ? "" : s.replace("\"", "\"\"")) + "\""; }

    private static String json(String s) { return s.replace("\\", "\\\\").replace("\"", "\\\""); }

    private static String unjson(String s) { return s.replace("\\\"", "\"").replace("\\\\", "\\"); }

    private static String uri(String path) { return path.replace(" ", "%20").replace(",", "%2C"); }

    private static final String CSS = """
:root{--bg:#f6f7f9;--fg:#1c1f24;--card:#fff;--line:#dde1e6;--pass:#1a7f37;--fail:#cf222e;--skip:#8a6d00;--mute:#6b7380}
@media(prefers-color-scheme:dark){:root{--bg:#14161a;--fg:#e6e8eb;--card:#1d2026;--line:#2e333b;--pass:#3fb950;--fail:#ff7b72;--skip:#d4a72c;--mute:#9aa3b0}}
body{margin:0;padding:24px 16px;background:var(--bg);color:var(--fg);font:15px/1.5 system-ui,Segoe UI,sans-serif}
main{max-width:1100px;margin:auto}h1{margin:0 0 4px}h2{margin:28px 0 8px}.meta{color:var(--mute);font-size:13px}a{color:inherit}
.sum{display:flex;gap:12px;flex-wrap:wrap;margin:16px 0}.sum div{background:var(--card);border:1px solid var(--line);border-radius:8px;padding:10px 16px}
.sum b{font-size:22px;display:block}.card{background:var(--card);border:1px solid var(--line);border-left:4px solid var(--line);border-radius:8px;padding:12px 16px;margin:12px 0}
.card.passed{border-left-color:var(--pass)}.card.failed{border-left-color:var(--fail)}.card.skipped{border-left-color:var(--skip)}
.head{display:flex;gap:10px;align-items:baseline;flex-wrap:wrap}.badge{font-size:12px;text-transform:uppercase;padding:2px 8px;border-radius:99px;border:1px solid currentColor}
.badge.passed{color:var(--pass)}.badge.failed{color:var(--fail)}.badge.skipped{color:var(--skip)}
pre{background:var(--bg);padding:10px;border-radius:6px;overflow:auto;white-space:pre-wrap}pre.err{color:var(--fail)}
.shots{display:flex;gap:12px;flex-wrap:wrap;margin-top:10px}figure{margin:0}img{max-width:300px;border:1px solid var(--line);border-radius:6px;cursor:zoom-in}
img.big{max-width:100%;cursor:zoom-out}figcaption{font-size:12px;color:var(--mute)}
table{width:100%;border-collapse:collapse;background:var(--card);border:1px solid var(--line)}th,td{text-align:left;padding:8px 12px;border-bottom:1px solid var(--line);vertical-align:top}
.passed-t{color:var(--pass)}.failed-t{color:var(--fail)}.skipped-t{color:var(--skip)}
""";
}
