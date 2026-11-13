package com.dilly.donutpay;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The TOP DONATORS overlay from the video: gold-bordered panel, podium (1st with a crown in the middle,
 * 2nd/3rd on the sides), a #4-#8 list and a "TOTAL RAISED" footer. Toggle with the keybind
 * (or /donutpay leaderboard); it also pops up by itself for a few seconds after a $100M+ show.
 */
final class Leaderboard {
    private Leaderboard() {}

    private static final int PW = 200, PH = 188;
    private static final int GOLD = 0xFFD700, GREEN = 0x55FF55, WHITE = 0xFFFFFF, GRAY = 0xAAAAAA;

    private static boolean open, preview;
    private static long openedAt, closedAt, autoCloseAt;
    private static String lastDonor = "";
    private static long lastDonorAt;
    /** Updated every tick by the client with the bound key's name. */
    static String keyHint = "O";

    private static final class Row {
        final String name; final long amount;
        Row(String name, long amount) { this.name = name; this.amount = amount; }
    }

    static boolean isOpen() { return open; }

    static void toggle() { if (open) close(); else show(0, false); }

    /** autoMs > 0 closes it by itself; preview uses made-up demo donors. */
    static void show(long autoMs, boolean prev) {
        long now = System.currentTimeMillis();
        open = true; preview = prev; openedAt = now;
        autoCloseAt = autoMs > 0 ? now + autoMs : 0;
    }

    static void close() {
        if (!open) return;
        open = false;
        closedAt = System.currentTimeMillis();
    }

    static void onPayment(String name) { lastDonor = name; lastDonorAt = System.currentTimeMillis(); }

    // ================================================================ render
    static void render(DrawContext ctx, TextRenderer tr, int w, int h, long now) {
        if (open && autoCloseAt > 0 && now > autoCloseAt) close();
        double raw = open ? clamp01((now - openedAt) / 350.0) : 1 - clamp01((now - closedAt) / 220.0);
        if (!open && raw <= 0) return;
        double a = PaymentHud.ease(raw);
        long t = open ? now - openedAt : 99999;

        List<Row> rows = new ArrayList<>();
        long total;
        if (preview) {
            String[] n = {"DemoWhale", "NightOwl", "CoinGoblin", "Stacks_4_Days", "PixelBaron", "LuckyLlama", "TheRealMoney", "MrSteady"};
            long[] v = {10_100_000_000L, 10_000_000_000L, 5_000_000_000L, 4_000_000_000L, 3_010_000_000L, 2_890_000_000L, 2_660_000_000L, 2_280_000_000L};
            long sum = 0;
            for (int i = 0; i < n.length; i++) { rows.add(new Row(n[i], v[i])); sum += v[i]; }
            total = sum + 4_320_000_000L;
        } else {
            for (Map.Entry<String, Long> e : Stats.get().topPayers(8)) rows.add(new Row(e.getKey(), e.getValue()));
            total = Stats.get().lifetimeTotal;
        }

        // dim the world behind the board
        ctx.fill(0, 0, w, h, fa(0.45 * a, 0x000000));

        double fit = Math.min(1.0, Math.min((h - 8) / (double) PH, (w - 8) / (double) PW));
        double sc = fit * (0.86 + 0.14 * PaymentHud.easeOutBack(raw));
        Matrix3x2fStack ms = ctx.getMatrices();
        ms.pushMatrix();
        ms.translate(w / 2f, h / 2f);
        ms.scale((float) sc, (float) sc);
        ms.translate(-PW / 2f, -PH / 2f);

        // panel + gold border (outer bright, inner dark) + travelling shimmer
        ctx.fill(0, 0, PW, PH, fa(0.97 * a, 0x0B0B14));
        border(ctx, 0, 0, PW, PH, fa(a, 0xD4B106));
        border(ctx, 2, 2, PW - 2, PH - 2, fa(0.8 * a, 0x5A4A08));
        shimmer(ctx, now, a);

        // title
        String title = "\u2605 TOP DONATORS \u2605";
        text(ctx, tr, title, PW / 2 - tr.getWidth(title) / 2, 5, ta(a, GOLD));

        // ---- podium
        final int yb = 87;
        int[] colX = {100, 38, 162}, pedH = {30, 22, 16}, pedW = {56, 50, 50}, delay = {500, 300, 150};
        int[] top = {0xFFFFD700, 0xFFDADADA, 0xFFD08A4E}, bot = {0xFFB8860B, 0xFF9A9A9A, 0xFF8A5222};
        String[] lab = {"1ST", "2ND", "3RD"};
        for (int p = 0; p < 3; p++) {
            Row row = p < rows.size() ? rows.get(p) : null;
            double pp = PaymentHud.ease(clamp01((t - delay[p]) / 450.0));
            int ph = (int) Math.round(pedH[p] * pp), px = colX[p] - pedW[p] / 2;
            if (ph > 0) {
                ctx.fillGradient(px, yb - ph, px + pedW[p], yb, withA(top[p], a), withA(bot[p], a));
                ctx.fill(px, yb - ph, px + pedW[p], yb - ph + 1, fa(a * 0.7, 0xFFFFFF));
                if (ph >= 12) text(ctx, tr, lab[p], colX[p] - tr.getWidth(lab[p]) / 2, yb - ph / 2 - 4, ta(a, 0x1A1A1A), false);
            }
            if (row == null) continue;
            int hs = p == 0 ? 26 : 22;
            int hy = yb - ph - hs - 2 - (p == 0 ? (int) Math.round(1.5 * Math.sin(now / 300.0)) : 0);
            double hp = clamp01((t - delay[p] - 150) / 300.0);
            if (hp > 0) {
                boolean hot = row.name.equalsIgnoreCase(lastDonor) && now - lastDonorAt < 3000;
                int frame = hot ? fa(0.6 + 0.4 * Math.sin(now / 90.0), GREEN) : fa(a, top[p] & 0xFFFFFF);
                ctx.fill(colX[p] - hs / 2 - 1, hy - 1, colX[p] + hs / 2 + 1, hy + hs + 1, frame);
                PlayerSkinDrawer.draw(ctx, PaymentHud.skin(row.name), colX[p] - hs / 2, hy, hs);
                if (p == 0) crown(ctx, colX[p], hy, hs, now);
            }
            double np = clamp01((t - delay[p] - 300) / 300.0);
            if (np > 0) {
                String nm = tr.trimToWidth(row.name, 54);
                text(ctx, tr, nm, colX[p] - tr.getWidth(nm) / 2, yb + 4, ta(np * a, WHITE));
                String am = Stats.fmt((long) (row.amount * PaymentHud.ease(clamp01((t - delay[p] - 350) / 900.0))));
                text(ctx, tr, am, colX[p] - tr.getWidth(am) / 2, yb + 14, ta(np * a, GREEN));
            }
        }
        if (t > 1100 && !rows.isEmpty()) sparkles(ctx, 100, yb - pedH[0] - 28, now, a);

        // ---- #4..#8
        int ly = 113;
        if (rows.isEmpty()) {
            String msg = "No donations yet - go get paid!";
            text(ctx, tr, msg, PW / 2 - tr.getWidth(msg) / 2, ly + 20, ta(a, GRAY));
        }
        for (int i = 3; i < 8 && i < rows.size(); i++) {
            Row row = rows.get(i);
            int ry = ly + (i - 3) * 11;
            double rp = PaymentHud.ease(clamp01((t - (700 + (i - 3) * 80L)) / 300.0));
            if (rp <= 0) continue;
            int off = (int) Math.round((1 - rp) * 40);
            double ra = rp * a;
            boolean hot = row.name.equalsIgnoreCase(lastDonor) && now - lastDonorAt < 3000;
            if (hot) ctx.fill(6 + off, ry - 1, PW - 6 + off, ry + 10, fa((0.18 + 0.12 * Math.sin(now / 100.0)) * ra, GREEN));
            else if (i % 2 == 0) ctx.fill(6 + off, ry - 1, PW - 6 + off, ry + 10, fa(0.07 * ra, 0xFFFFFF));
            text(ctx, tr, "#" + (i + 1), 10 + off, ry, ta(ra, GRAY));
            PlayerSkinDrawer.draw(ctx, PaymentHud.skin(row.name), 28 + off, ry, 8);
            text(ctx, tr, tr.trimToWidth(row.name, 92), 40 + off, ry, ta(ra, WHITE));
            String am = Stats.fmt((long) (row.amount * PaymentHud.ease(clamp01((t - (900 + (i - 3) * 80L)) / 800.0))));
            text(ctx, tr, am, PW - 10 - tr.getWidth(am) + off, ry, ta(ra, GREEN));
        }

        // ---- footer
        ctx.fill(6, 170, PW - 6, 171, fa(0.35 * a, 0xD4B106));
        text(ctx, tr, "TOTAL RAISED: " + Stats.fmt(total), 8, 175, ta(a, 0xE0A800));
        String hint = autoCloseAt > 0 && open ? (preview ? "preview" : "") : "[" + keyHint + "] close";
        if (!hint.isEmpty()) text(ctx, tr, hint, PW - 8 - tr.getWidth(hint), 175, ta(a, 0x888888));
        if (autoCloseAt > 0 && open) {                       // auto-close countdown bar
            double rem = clamp01((autoCloseAt - now) / (double) Math.max(1, autoCloseAt - openedAt));
            ctx.fill(3, PH - 4, 3 + (int) ((PW - 6) * rem), PH - 3, fa(0.8 * a, GOLD));
        }
        ms.popMatrix();
    }

    // ================================================================ pieces
    private static void crown(DrawContext ctx, int cx, int headTop, int hs, long now) {
        int bob = (int) Math.round(Math.sin(now / 250.0) * 1.2);
        int bw = (int) (hs * 0.72), bx = cx - bw / 2, by = headTop - 2 + bob;
        ctx.fill(bx, by - 3, bx + bw, by + 1, 0xFFFFD700);
        ctx.fill(bx, by - 3, bx + bw, by - 2, 0xFFFFF2A0);
        int pw = Math.max(3, bw / 5);
        for (int i = 0; i < 3; i++) {
            int px = bx + i * (bw - pw) / 2;
            ctx.fill(px, by - 8, px + pw, by - 3, 0xFFFFD700);
            ctx.fill(px + pw / 2, by - 10, px + pw / 2 + 2, by - 8, 0xFFFFF2A0);
        }
        ctx.fill(cx - 1, by - 2, cx + 1, by, 0xFFD83A32);
    }

    private static void sparkles(DrawContext ctx, int cx, int cy, long now, double a) {
        for (int i = 0; i < 7; i++) {
            double ph = (now / 900.0 + i * 0.37) % 1.0;
            double ang = i * 2.4 + now / 1400.0;
            int x = (int) (cx + Math.cos(ang) * (16 + 14 * ph)), y = (int) (cy + Math.sin(ang) * (12 + 10 * ph));
            int col = fa((1 - ph) * a, 0xFFF2A0);
            ctx.fill(x - 1, y, x + 2, y + 1, col);
            ctx.fill(x, y - 1, x + 1, y + 2, col);
        }
    }

    private static void shimmer(DrawContext ctx, long now, double a) {
        double per = 2.0 * (PW + PH), s = (now / 5.0) % per;
        for (int d = 0; d < 30; d += 2) {
            double pos = (s + d) % per;
            int x, y;
            if (pos < PW) { x = (int) pos; y = 0; }
            else if (pos < PW + PH) { x = PW - 2; y = (int) (pos - PW); }
            else if (pos < 2 * PW + PH) { x = (int) (PW - (pos - PW - PH)); y = PH - 2; }
            else { x = 0; y = (int) (PH - (pos - 2 * PW - PH)); }
            ctx.fill(x, y, x + 2, y + 2, fa((1 - d / 30.0) * 0.9 * a, 0xFFF6C0));
        }
    }

    private static void border(DrawContext ctx, int x1, int y1, int x2, int y2, int col) {
        ctx.fill(x1, y1, x2, y1 + 1, col);
        ctx.fill(x1, y2 - 1, x2, y2, col);
        ctx.fill(x1, y1, x1 + 1, y2, col);
        ctx.fill(x2 - 1, y1, x2, y2, col);
    }

    // ================================================================ helpers
    private static void text(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color) { text(ctx, tr, s, x, y, color, true); }

    private static void text(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color, boolean shadow) {
        ctx.drawText(tr, s, x, y, color, shadow);
    }

    private static double clamp01(double v) { return Math.max(0, Math.min(1, v)); }

    /** fill colour, alpha 0 allowed */
    private static int fa(double alpha, int rgb) {
        return (Math.max(0, Math.min(255, (int) (alpha * 255))) << 24) | (rgb & 0xFFFFFF);
    }

    /** text colour, alpha never 0 (vanilla treats 0 as opaque) */
    private static int ta(double alpha, int rgb) { return (PaymentHud.clampA(alpha) << 24) | (rgb & 0xFFFFFF); }

    private static int withA(int argb, double a) { return fa(a, argb & 0xFFFFFF); }
}
