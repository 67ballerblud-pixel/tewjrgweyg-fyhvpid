package com.dilly.donutpay;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

import java.util.Locale;
import java.util.Random;

/**
 * The per-tier full-screen shows, modelled on the DrDonut video.
 *   $1M coin rain | $5M deal-with-it shades | $10M breaking news | $50M wanted poster | $100M energy orb
 *   $500M legendary coin | $1B rocket | $5B jackpot wheel | $10B supernova -> black hole
 *   $50B god of donut (sunburst + laser eyes) | $100B "JK you're rich" gold flood | $1T legend (crown + fireworks)
 */
final class Shows {
    private Shows() {}

    private static final int GOLD = 0xFFD700, ORANGE = 0xFFAA00, GREEN = 0x55FF55, WHITE = 0xFFFFFF,
        PURPLE = 0xAA55FF, RED = 0xFF5555, GRAY = 0xAAAAAA, CREAM = 0xFFF4D6, PINK = 0xFF4D6D;
    private static final int[] GOLDS = {GOLD, ORANGE, 0xFFF2A0, 0xFFFFFF};
    private static final int[] RAINBOW = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xFF55FF};
    private static final String[] WORDS = {"CRACKED", "HUGE", "INSANE", "RICH", "MONSTER", "UNREAL", "LEGEND", "WHALE"};

    private static final class G {
        final DrawContext ctx; final TextRenderer tr; final PaymentHud.Note n; final Matrix3x2fStack ms;
        final int w, h, cx, dur; final long el; final double fade; final SkinTextures skin;
        G(DrawContext ctx, TextRenderer tr, PaymentHud.Note n, long now, int w, int h) {
            this.ctx = ctx; this.tr = tr; this.n = n; this.ms = ctx.getMatrices();
            this.w = w; this.h = h; this.cx = w / 2; this.dur = n.tier.ms; this.el = now - n.start;
            this.fade = Math.max(0, Math.min(1, Math.min(el / 250.0, (dur - el) / 500.0)));
            this.skin = PaymentHud.skin(n.name);
        }
    }

    // ================================================================ entry point
    static void draw(DrawContext ctx, TextRenderer tr, PaymentHud.Note n, long now, int w, int h) {
        G g = new G(ctx, tr, n, now, w, h);
        double amp = shakeAmp(g);
        g.ms.pushMatrix();
        if (amp > 0.2) {
            Random r = new Random((g.el / 35) * 31 + n.start);
            g.ms.translate((float) ((r.nextDouble() - 0.5) * 2 * amp), (float) ((r.nextDouble() - 0.5) * 2 * amp));
        }
        switch (n.tier) {
            case COIN_RAIN -> coinRain(g);
            case DEAL_WITH_IT -> dealWithIt(g);
            case BREAKING_NEWS -> breakingNews(g);
            case WANTED -> wanted(g);
            case BIG -> big(g);
            case LEGENDARY -> legendary(g);
            case MEGA -> mega(g);
            case JACKPOT -> jackpot(g);
            case SUPERNOVA -> supernova(g);
            case GOD -> god(g);
            case RICH -> rich(g);
            case LEGEND -> legend(g);
            case SLOTS -> slots(g);
            case STONKS -> stonks(g);
            case LIGHTNING -> lightning(g);
            case DIAMOND -> diamond(g);
            case VAULT -> vault(g);
            case METEOR -> meteor(g);
            case UFO -> ufo(g);
            case PORTAL -> portal(g);
            case ECLIPSE -> eclipse(g);
            case STORM -> storm(g);
            case BIGBANG -> bigbang(g);
            case FIREWORKS -> fireworks(g);
        }
        g.ms.popMatrix();
        if (n.tier.cinema) {                               // cinematic letterbox bars on the big shows
            double bp = Math.min(PaymentHud.ease(clamp01(g.el / 450.0)), PaymentHud.ease(clamp01((g.dur - g.el) / 450.0)));
            int bh = (int) (h * 0.075 * bp);
            ctx.fill(0, 0, w, bh, 0xFF000000);
            ctx.fill(0, h - bh, w, h, 0xFF000000);
        }
        if (n.preview) text(g, "[PREVIEW]", g.cx, 6, 1f, GRAY, g.fade);
    }

    private static double shakeAmp(G g) {
        long el = g.el;
        switch (g.n.tier) {
            case SUPERNOVA:
                if (el < 2000) return el / 2000.0 * 3;
                if (el < 3000) return 7 * (1 - (el - 2000) / 1000.0);
                return 0;
            case GOD: { long t = el - 1800; return t > 0 && t < 1500 ? 2.5 * (1 - t / 1500.0) : 0; }
            case LEGEND: return el < 500 ? 4 * (1 - el / 500.0) : 0;
            case WANTED: { long t = el - 1300; return t > 0 && t < 350 ? 3 * (1 - t / 350.0) : 0; }
            default: return 0;
        }
    }

    // ================================================================ $1M  COIN RAIN
    private static void coinRain(G g) {
        PaymentHud.cue(g.n, 0, g.el, 0, "minecraft:block.amethyst_block.chime", 0.9f, 1.6f);
        PaymentHud.cue(g.n, 1, g.el, 250, "minecraft:entity.experience_orb.pickup", 0.8f, 1.4f);
        PaymentHud.cue(g.n, 2, g.el, 500, "minecraft:entity.experience_orb.pickup", 0.8f, 1.8f);
        coins(g, 46, 0);
        double pop = PaymentHud.easeOutBack(clamp01(g.el / 350.0));
        int y = (int) (g.h * 0.22);
        text(g, "+" + Stats.fmt(g.n.amount), g.cx, y, (float) (2.0 * pop), GOLD, g.fade);
        text(g, "from " + g.n.name, g.cx, y + 22, 1.1f, WHITE, g.fade);
    }

    // ================================================================ $5M  DEAL WITH IT
    private static void dealWithIt(G g) {
        long el = g.el; int S = 44, cy = (int) (g.h * 0.34);
        double band = Math.min(PaymentHud.ease(clamp01(el / 300.0)), PaymentHud.ease(clamp01((g.dur - el) / 300.0)));
        fillA(g, 0, cy - S / 2 - 8, g.w, cy + S / 2 + 46, 0.55 * band, 0x000000);
        double p = bounce(clamp01(el / 800.0));
        int hx = g.cx - S / 2, hy = (int) (-S + (cy - S / 2 + S) * p);
        head(g, hx, hy, S);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.player.levelup", 0.7f, 1.3f);
        if (el >= 900) {
            glasses(g, hx, hy, S, bounce(clamp01((el - 900) / 500.0)));
            PaymentHud.cue(g.n, 1, el, 1250, "minecraft:block.anvil.land", 0.5f, 1.7f);
        }
        if (el >= 1400) {
            double pop = PaymentHud.easeOutBack(clamp01((el - 1400) / 350.0));
            rainbowText(g, "DEAL WITH IT", g.cx, cy + S / 2 + 6, (float) (2.0 * pop), el);
            text(g, g.n.name + " donated " + Stats.fmt(g.n.amount), g.cx, cy + S / 2 + 30, 1.1f, WHITE, g.fade);
            PaymentHud.cue(g.n, 2, el, 1400, "minecraft:ui.toast.challenge_complete", 0.8f, 1.0f);
            confetti(g, 36, 1400, RAINBOW);
        }
    }

    // ================================================================ $10M  BREAKING NEWS
    private static void breakingNews(G g) {
        long el = g.el;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.note_block.pling", 0.9f, 1.0f);
        PaymentHud.cue(g.n, 1, el, 150, "minecraft:block.note_block.pling", 0.9f, 1.26f);
        PaymentHud.cue(g.n, 2, el, 300, "minecraft:block.note_block.pling", 0.9f, 1.5f);
        if (el < 200) fillA(g, 0, 0, g.w, g.h, (1 - el / 200.0) * 0.55, 0xFFFFFF);
        if (el >= 420 && el < 540) fillA(g, 0, 0, g.w, g.h, (1 - (el - 420) / 120.0) * 0.35, 0xFFFFFF);

        double p = Math.min(PaymentHud.ease(clamp01(el / 380.0)), PaymentHud.ease(clamp01((g.dur - el) / 380.0)));
        final int rowH = 15, tickH = 13, labelW = 100;
        int top = g.h - 32 - rowH - tickH + (int) ((1 - p) * (rowH + tickH + 50));
        boolean blink = (el / 250) % 2 == 0;

        // anchor head
        g.ctx.fill(4, top - 36, 40, top, 0xFFFFFFFF);
        head(g, 6, top - 34, 32);
        g.ctx.fill(4, top - 46, 28, top - 36, 0xFF000000 | (blink ? 0xD01010 : 0x801010));
        g.ctx.drawText(g.tr, "LIVE", 6, top - 44, 0xFFFFFFFF, false);

        g.ctx.fill(0, top, labelW, top + rowH, 0xFF000000 | (blink ? 0xD01010 : 0xFF3030));
        g.ctx.drawText(g.tr, "BREAKING NEWS", 6, top + 4, 0xFFFFFFFF, true);
        g.ctx.fill(labelW, top, g.w, top + rowH, 0xFFF2F2F2);
        String head = g.n.name.toUpperCase(Locale.ROOT) + " DONATES " + Stats.fmt(g.n.amount);
        g.ctx.drawText(g.tr, head, labelW + 6, top + 4, 0xFF111111, false);

        int ty = top + rowH;
        g.ctx.fill(0, ty, g.w, ty + tickH, 0xFF0A1F5C);
        g.ctx.fill(0, ty, g.w, ty + 1, 0xFF000000 | GOLD);
        String tick = "+++ ECONOMY IN SHOCK +++ " + g.n.name.toUpperCase(Locale.ROOT) + " JUST DONATED " + Stats.fmt(g.n.amount)
            + " +++ EXPERTS STUNNED +++ MARKETS GO WILD +++ ";
        int tw = Math.max(1, g.tr.getWidth(tick));
        int off = (int) ((el * 0.10) % tw);
        g.ctx.enableScissor(0, ty, g.w, ty + tickH);
        for (int x = -off; x < g.w; x += tw) g.ctx.drawText(g.tr, tick, x, ty + 3, 0xFFFFFFFF, false);
        g.ctx.disableScissor();

        double pop = PaymentHud.easeOutBack(clamp01(el / 400.0));
        text(g, Stats.fmt(g.n.amount), g.cx, (int) (g.h * 0.26), (float) (3.0 * pop), GOLD, g.fade);
        text(g, "from " + g.n.name, g.cx, (int) (g.h * 0.26) + 30, 1.2f, WHITE, g.fade);
    }

    // ================================================================ $50M  WANTED
    private static void wanted(G g) {
        long el = g.el;
        final int W = 100, H = 138;
        double slide = Math.min(PaymentHud.ease(clamp01(el / 450.0)), PaymentHud.ease(clamp01((g.dur - el) / 400.0)));
        int topY = (int) (-(H + 20) + (H + 20 + g.h * 0.08) * slide);
        double ang = 0.55 * Math.exp(-el / 520.0) * Math.cos(el / 120.0);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.item_frame.place", 1.0f, 0.8f);

        g.ms.pushMatrix();
        g.ms.translate((float) g.cx, (float) topY);
        g.ms.rotate((float) ang);
        g.ctx.fill(-W / 2 + 4, 4, W / 2 + 4, H + 4, 0x55000000);
        g.ctx.fill(-W / 2, 0, W / 2, H, 0xFFE6CF9A);
        g.ctx.fill(-W / 2, 0, W / 2, 2, 0xFF8B5A2B);
        g.ctx.fill(-W / 2, H - 2, W / 2, H, 0xFF8B5A2B);
        g.ctx.fill(-W / 2, 0, -W / 2 + 2, H, 0xFF8B5A2B);
        g.ctx.fill(W / 2 - 2, 0, W / 2, H, 0xFF8B5A2B);
        g.ctx.fill(-3, -5, 3, 1, 0xFF444444);

        textNS(g, "WANTED", 0, 8, 1.9f, 0x3B2412, 1);
        g.ctx.fill(-25, 28, 25, 78, 0xFF5B3A1A);
        PlayerSkinDrawer.draw(g.ctx, g.skin, -22, 31, 44);
        textNS(g, "DEAD OR ALIVE", 0, 82, 0.8f, 0x3B2412, 1);
        textNS(g, g.n.name, 0, 94, fit(g, g.n.name, 1.1f, W - 12), 0x3B2412, 1);
        textNS(g, "REWARD", 0, 106, 0.9f, 0x3B2412, 1);
        textNS(g, Stats.fmt(g.n.amount), 0, 116, fit(g, Stats.fmt(g.n.amount), 1.8f, W - 12), 0x9B1C1C, 1);

        if (el >= 1300) {
            PaymentHud.cue(g.n, 1, el, 1300, "minecraft:block.anvil.land", 1.0f, 0.8f);
            double k = clamp01((el - 1300) / 220.0);
            float s = (float) (1 + 2.5 * (1 - PaymentHud.ease(k)));
            g.ms.pushMatrix();
            g.ms.translate(0f, 72f);
            g.ms.rotate(-0.35f);
            g.ms.scale(s * 2f, s * 2f);
            int col = targb(k, 0xCC1111);
            int rc = fillArgb(k, 0xCC1111);
            g.ctx.fill(-15, -7, 15, -6, rc);
            g.ctx.fill(-15, 6, 15, 7, rc);
            g.ctx.fill(-15, -7, -14, 7, rc);
            g.ctx.fill(14, -7, 15, 7, rc);
            g.ctx.drawText(g.tr, "PAID", -g.tr.getWidth("PAID") / 2, -4, col, false);
            g.ms.popMatrix();
        }
        g.ms.popMatrix();
        if (el > 1300) burst(g, g.cx, (int) (g.h * 0.08 + 72), 26, 1300, 90, GOLDS, 0x57A7L);
    }

    // ================================================================ $100M  ENERGY ORB
    private static void big(G g) {
        long el = g.el; double fade = g.fade;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:ui.toast.challenge_complete", 1.0f, 1.0f);
        glow(g, 0.30);
        int cx = g.cx, cy = (int) (g.h * 0.30);
        // head orb with a spinning dotted ring (video: the orb that later collapses)
        int oy = cy - 36;
        double pulse = 1 + 0.06 * Math.sin(el / 120.0);
        ellipse(g.ctx, cx, oy, 17 * pulse, 17 * pulse, argb(0.35 * fade, GOLD));
        ellipse(g.ctx, cx, oy, 14, 14, argb(0.55 * fade, 0xE0B000));
        head(g, cx - 10, oy - 10, 20);
        ring(g.ctx, cx, oy, 23, 16, el / 400.0, 2, argb(fade, 0xFFFFFF));
        ring(g.ctx, cx, oy, 30, 22, -el / 520.0, 1, argb(0.6 * fade, GOLD));

        double pop = PaymentHud.easeOutBack(clamp01(el / 350.0));
        int ta = PaymentHud.clampA(fade);
        PaymentHud.scaledCentered(g.ctx, g.tr, Stats.fmt(g.n.amount) + " from " + g.n.name, cx, cy, (float) (2.0 * pop), (ta << 24) | GOLD);
        PaymentHud.scaledCentered(g.ctx, g.tr, PaymentHud.scramble(PaymentHud.energy(g.n.amount), el, g.n.start + el), cx, cy + 22, 1.3f, (ta << 24) | GREEN);
        sparkles(g, cx, cy, 28);
    }

    // ================================================================ $500M  LEGENDARY COIN
    private static void legendary(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.end_portal.spawn", 0.5f, 1.2f);
        PaymentHud.cue(g.n, 1, el, 700, "minecraft:entity.player.levelup", 0.9f, 0.8f);
        fillA(g, 0, 0, g.w, g.h, 0.62 * f, 0x000000);
        stars(g, 70, f);
        int bw = (int) Math.min(g.w * 0.34, 150);
        fillA(g, cx - bw, 0, cx + bw, g.h, 0.10 * f, 0xFFFFFF);

        boolean fl = (el / 110) % 2 == 0;
        String title = "\u2620 LEGENDARY DONATION \u2620";
        text(g, title, cx, (int) (g.h * 0.08), fit(g, title, 1.6f, g.w - 20), fl ? GOLD : 0xFFF3A0, f);
        text(g, "1 IN 10,000 DROP", cx, (int) (g.h * 0.08) + 18, 0.9f, RED, f);

        double rise = PaymentHud.ease(clamp01(el / 700.0));
        int ry = (int) Math.min(34, g.h * 0.12), coinY = (int) (g.h * 0.40) + (int) ((1 - rise) * 90);
        double spin = el / 230.0;
        double c = Math.abs(Math.cos(spin));
        coinBody(g, cx, coinY, Math.max(2, 22 * c), ry, c);
        ring(g.ctx, cx, coinY, ry + 14 + 3 * Math.sin(el / 150.0), 20, el / 500.0, 2, argb(0.7 * f, GOLD));

        int hy = coinY + ry + 6;
        head(g, cx - 8, hy, 16);
        text(g, g.n.name, cx, hy + 20, 1.4f, WHITE, f);
        text(g, "JUST BROKE THE LEADERBOARD", cx, hy + 34, 0.9f, RED, f);
        double pop = PaymentHud.easeOutBack(clamp01((el - 500) / 400.0));
        text(g, Stats.fmt(g.n.amount), cx, hy + 48, (float) (2.2 * Math.max(0.01, pop)), GOLD, f);
        sparkles(g, cx, coinY, 40);
    }

    // ================================================================ $1B  ROCKET ("TO THE MOON")
    private static void mega(G g) {
        long el = g.el; double fade = g.fade;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.firework_rocket.launch", 1.0f, 0.9f);
        PaymentHud.cue(g.n, 1, el, 900, "minecraft:entity.firework_rocket.blast", 1.0f, 0.8f);
        PaymentHud.cue(g.n, 2, el, 1400, "minecraft:ui.toast.challenge_complete", 0.9f, 1.0f);
        glow(g, 0.43);
        int cx = g.cx, cy = (int) (g.h * 0.34);
        boolean yeet = Math.floorMod(Long.hashCode(g.n.start), 2) == 1;
        String phrase = yeet ? "YEET TO THE MOON" : "TO THE MOON";
        int phraseCol = yeet ? GREEN : PURPLE;

        bubbles(g);
        rocket(g, cx, cy);
        headBurst(g, cx, cy);

        int ta = PaymentHud.clampA(fade);
        double pop = PaymentHud.easeOutBack(clamp01(el / 450.0));
        double pulse = 1 + 0.035 * Math.sin(el / 90.0);
        PaymentHud.scaledCentered(g.ctx, g.tr, "\u2605 " + phrase + " \u2605", cx, cy - 30, 1.5f, (ta << 24) | phraseCol);
        PaymentHud.scaledCentered(g.ctx, g.tr, Stats.fmt(g.n.amount), cx, cy - 4, (float) (4.0 * pop * pulse), (ta << 24) | GOLD);
        PaymentHud.scaledCentered(g.ctx, g.tr, "from " + g.n.name, cx, cy + 36, 1.6f, (ta << 24) | WHITE);
        PaymentHud.scaledCentered(g.ctx, g.tr, PaymentHud.energy(g.n.amount), cx, cy + 54, 1.1f, (ta << 24) | ORANGE);
        sparkles(g, cx, cy, 36);
    }

    // ================================================================ $5B  JACKPOT WHEEL
    private static void jackpot(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx;
        fillA(g, 0, 0, g.w, g.h, 0.66 * f, 0x000000);
        for (int i = 0; i < g.w / 10; i++) {
            int x = i * 10 + 4;
            int col = ((i + el / 140) % 2 == 0) ? 0xFFFFD700 : 0xFF553300;
            g.ctx.fill(x, 3, x + 3, 6, col);
            g.ctx.fill(x, g.h - 6, x + 3, g.h - 3, col);
        }
        int wy = (int) (g.h * 0.50), R = (int) Math.min(54, g.h * 0.20);
        double t = clamp01(el / 3600.0);
        double rot = 14 * (1 - Math.pow(1 - t, 3));
        if (el < 3500) PaymentHud.tick(g.n, el, 150, "minecraft:block.note_block.hat", 0.6f, 1.4f);
        for (int ring = 0; ring < 8; ring++) {
            double r = 6 + ring * (R - 6) / 7.0;
            int dots = Math.max(6, (int) (r * 0.9));
            for (int i = 0; i < dots; i++) {
                double ang = i * 2 * Math.PI / dots + rot;
                int x = (int) (cx + Math.cos(ang) * r), y = (int) (wy + Math.sin(ang) * r);
                int col = hsv(i / (double) dots + ring * 0.03, 0.9, 1.0);
                g.ctx.fill(x, y, x + 2, y + 2, argb(f, col));
            }
        }
        ellipse(g.ctx, cx, wy, 4, 4, argb(f, GOLD));
        for (int k = 0; k < 7; k++) g.ctx.fill(cx - (6 - k), wy - R - 10 + k, cx + (6 - k) + 1, wy - R - 9 + k, 0xFFFFD700);

        double pulse = 1 + 0.05 * Math.sin(el / 90.0);
        boolean alt = (el / 180) % 2 == 0;
        text(g, "$$$ JACKPOT $$$", cx, wy - R - 36, (float) (1.8 * pulse), alt ? GREEN : GOLD, f);
        if (el > 3600) {
            PaymentHud.cue(g.n, 0, el, 3600, "minecraft:ui.toast.challenge_complete", 1.0f, 1.0f);
            PaymentHud.cue(g.n, 1, el, 3650, "minecraft:entity.player.levelup", 1.0f, 1.2f);
            double pop = PaymentHud.easeOutBack(clamp01((el - 3600) / 350.0));
            text(g, g.n.name + " WINS " + Stats.fmt(g.n.amount), cx, wy + R + 12, (float) (1.6 * Math.max(0.01, pop)), alt ? GOLD : WHITE, f);
            confetti(g, 70, 3600, RAINBOW);
        } else {
            text(g, g.n.name + " spins the wheel...", cx, wy + R + 12, 1.1f, WHITE, f);
        }
    }

    // ================================================================ $10B  SUPERNOVA -> BLACK HOLE
    private static void supernova(G g) {
        long el = g.el; int cx = g.cx, cy = (int) (g.h * 0.42);
        double out = clamp01((g.dur - el) / 700.0);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.beacon.activate", 1.0f, 0.6f);
        if (el < 2000) PaymentHud.tick(g.n, el, 400, "minecraft:block.note_block.bit", 0.7f, (float) (0.6 + el / 1000.0));
        PaymentHud.cue(g.n, 1, el, 2000, "minecraft:entity.generic.explode", 1.0f, 0.5f);
        PaymentHud.cue(g.n, 2, el, 2000, "minecraft:entity.lightning_bolt.thunder", 0.8f, 0.7f);
        PaymentHud.cue(g.n, 3, el, 3000, "minecraft:block.end_portal.spawn", 0.6f, 0.6f);

        if (el < 2000) {                                   // phase 1: the star goes critical
            double k = el / 2000.0;
            fillA(g, 0, 0, g.w, g.h, 0.40 * k, 0x000000);
            stars(g, 50, k);
            double r = 14 + 26 * k * k;
            for (int i = 3; i >= 1; i--) ellipse(g.ctx, cx, cy, r + i * 5 + 2 * Math.sin(el / 60.0), r + i * 5 + 2 * Math.sin(el / 60.0), argb(0.10 + 0.04 * k, 0xFFC000));
            ellipse(g.ctx, cx, cy, r, r, argb(0.55, ORANGE));
            int hs = (int) (r * 1.3);
            head(g, cx - hs / 2, cy - hs / 2, hs);
            double ph = (el % 600) / 600.0;
            ring(g.ctx, cx, cy, r + ph * 70, 28, el / 300.0, 2, argb(1 - ph, 0xFFE080));
            boolean fl = (el / 130) % 2 == 0;
            int ty = (int) (g.h * 0.14);
            text(g, "\u26A0 STAR GOING CRITICAL \u26A0", cx, ty, 1.5f, fl ? GOLD : RED, 1);
            text(g, g.n.name + " JUST DROPPED " + Stats.fmt(g.n.amount), cx, ty + 17, 1.1f, WHITE, 1);
        } else if (el < 3000) {                            // phase 2: supernova flash
            double t = (el - 2000) / 1000.0;
            fillA(g, 0, 0, g.w, g.h, 0.4 + 0.4 * t, 0x000000);
            double rad = PaymentHud.ease(t) * Math.max(g.w, g.h) * 0.9;
            ellipse(g.ctx, cx, cy, rad, rad, argb(0.95, CREAM));
            double wash = t < 0.45 ? t / 0.45 : Math.max(0, 1 - (t - 0.45) / 0.55);
            fillA(g, 0, 0, g.w, g.h, 0.92 * wash, CREAM);
            ring(g.ctx, cx, cy, rad * 0.9, 90, 0, 2, argb(0.8 * (1 - t), 0xFFFFFF));
            ring(g.ctx, cx, cy, rad * 0.6, 60, 0.3, 2, argb(0.6 * (1 - t), 0xFFE080));
        } else {                                           // phase 3: black hole
            long t3 = el - 3000;
            fillA(g, 0, 0, g.w, g.h, 0.80 * out, 0x000000);
            stars(g, 90, out);
            Random r = new Random(g.n.start ^ 0xB1ACL);
            for (int i = 0; i < 46; i++) {
                double rs = 34 + r.nextDouble() * 110, sp = 0.8 + r.nextDouble() * 1.4, off = r.nextDouble() * 6.28;
                double life = 2400 + r.nextDouble() * 1200, ph = r.nextDouble() * life;
                double tt = ((t3 + ph) % life) / life;
                double rr = rs * (1 - tt), ang = off + tt * sp * 7;
                if (rr < 16) continue;
                int x = (int) (cx + Math.cos(ang) * rr), y = (int) (cy + Math.sin(ang) * rr);
                if (i % 3 == 0) {
                    int sz = Math.max(2, (int) (9 * (1 - tt)));
                    PlayerSkinDrawer.draw(g.ctx, g.skin, x - sz / 2, y - sz / 2, sz);
                } else {
                    g.ctx.fill(x, y, x + 2, y + 2, argb(out * (1 - tt * 0.5), WHITE));
                }
            }
            ellipse(g.ctx, cx, cy, 16, 16, 0xFFFFFFFF);
            ellipse(g.ctx, cx, cy, 15, 15, 0xFF000000);
            double ph = (t3 % 1500) / 1500.0;
            ring(g.ctx, cx, cy, 18 + ph * 30, 48, 0, 1, argb(out * (1 - ph) * 0.8, WHITE));
            if (t3 > 300) {
                double pop = PaymentHud.easeOutBack(clamp01((t3 - 300) / 400.0));
                text(g, Stats.fmt(g.n.amount) + " from " + g.n.name, cx, (int) (g.h * 0.14), (float) (1.9 * Math.max(0.01, pop)), GOLD, out);
                text(g, PaymentHud.energy(g.n.amount), cx, (int) (g.h * 0.14) + 24, 1.1f, WHITE, out);
            }
        }
    }

    // ================================================================ $50B  GOD OF DONUT SMP
    private static void god(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.40);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.ender_dragon.growl", 0.6f, 1.2f);
        PaymentHud.cue(g.n, 1, el, 1800, "minecraft:entity.warden.sonic_boom", 0.6f, 1.3f);
        fillA(g, 0, 0, g.w, g.h, 0.74 * f, 0x000000);
        stars(g, 70, f);
        int len = Math.max(g.w, g.h);
        rays(g, cx, cy, 0, len, 18, el / 5000.0, argb(0.45 * f, GOLD), 3);
        rays(g, cx, cy, 0, len, 18, el / 5000.0 + 0.04, argb(0.25 * f, GOLD), 2);

        double z = PaymentHud.easeOutBack(clamp01(el / 900.0));
        int HS = (int) ((26 + 70 * z) * (1 + 0.00003 * el) * (1 + 0.02 * Math.sin(el / 110.0)) * (0.5 + 0.5 * f));
        int R = (int) (HS * 0.72);
        ellipse(g.ctx, cx, cy, R + 6, R + 6, argb(0.22 * f, GOLD));
        ellipse(g.ctx, cx, cy, R, R, argb(0.55 * f, 0xE0B000));
        int hx = cx - HS / 2, hy = cy - HS / 2;
        head(g, hx, hy, HS);

        if (el > 1800) {                                    // laser eyes
            double a = 0.7 + 0.3 * Math.sin(el / 60.0);
            int ey = hy + (int) (HS * 0.47);
            laser(g, hx + (int) (HS * 0.28), ey, Math.atan2(1, -0.45), a);
            laser(g, hx + (int) (HS * 0.72), ey, Math.atan2(1, 0.45), a);
        }
        // floating hype words
        Random r = new Random(g.n.start ^ 0x60D5L);
        for (int i = 0; i < 10; i++) {
            double start = 500 + i * 450, life = 2200;
            double t = el - start;
            if (t < 0 || t > life) continue;
            double ang = r.nextDouble() * Math.PI * 2, dist = R + 22 + r.nextDouble() * 60;
            int x = (int) (cx + Math.cos(ang) * dist * 1.4), y = (int) (cy + Math.sin(ang) * dist - t / 40.0);
            double k = t / life;
            text(g, WORDS[i % WORDS.length], x, y, 0.9f, GOLD, Math.min(1, Math.min(k * 6, (1 - k) * 3)) * f);
        }
        int ty = cy + R + 10;
        text(g, "\u26A1 GOD OF DONUT SMP \u26A1", cx, ty, 1.4f, GOLD, f);
        text(g, g.n.name + " ASCENDED WITH " + Stats.fmt(g.n.amount), cx, ty + 17, 1.1f, WHITE, f);
        confetti(g, 40, 300, GOLDS);
    }

    // ================================================================ $100B  "JK. YOU'RE RICH."
    private static void rich(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.player.levelup", 1.0f, 0.9f);
        PaymentHud.cue(g.n, 1, el, 600, "minecraft:entity.villager.celebrate", 1.0f, 1.0f);
        PaymentHud.cue(g.n, 2, el, 1150, "minecraft:block.anvil.land", 0.6f, 1.5f);
        double p = PaymentHud.ease(clamp01(el / 700.0));
        g.ctx.fillGradient(0, 0, g.w, g.h, argb(0.92 * p * f, 0xFFC81E), argb(0.92 * p * f, 0xB8860B));
        rays(g, cx, (int) (g.h * 0.40), 0, Math.max(g.w, g.h), 14, el / 3000.0, argb(0.18 * p * f, 0xFFFFFF), 6);
        coins(g, 80, 0);

        int S = 38, cy = (int) (g.h * 0.36);
        double dp = bounce(clamp01(el / 800.0));
        int hx = cx - S / 2, hy = (int) (-S + (cy - S / 2 + S) * dp);
        g.ctx.fill(hx - 2, hy - 2, hx + S + 2, hy + S + 2, 0xFF3B2412);
        head(g, hx, hy, S);
        if (el >= 900) glasses(g, hx, hy, S, bounce(clamp01((el - 900) / 500.0)));
        if (el >= 600) {
            double pop = PaymentHud.easeOutBack(clamp01((el - 600) / 400.0));
            outlined(g, "JK. YOU'RE RICH.", cx, cy + S / 2 + 8, (float) (2.4 * Math.max(0.01, pop)), WHITE, 0x3B2412, f);
            outlined(g, g.n.name + "  +" + Stats.fmt(g.n.amount), cx, cy + S / 2 + 34, 1.5f, WHITE, 0x3B2412, f);
        }
    }

    // ================================================================ $1T  LEGEND (crown, rainbow sunburst, fireworks)
    private static void legend(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.36);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:ui.toast.challenge_complete", 1.0f, 0.8f);
        PaymentHud.cue(g.n, 1, el, 0, "minecraft:entity.ender_dragon.death", 0.4f, 1.4f);
        if (el < 350) fillA(g, 0, 0, g.w, g.h, (1 - el / 350.0) * 0.8, 0xFFFFFF);
        fillA(g, 0, 0, g.w, g.h, 0.62 * f, 0x000000);
        stars(g, 60, f);
        int len = Math.max(g.w, g.h);
        for (int i = 0; i < 16; i++)
            rays(g, cx, cy, 0, len, 1, el / 4500.0 + i * 2 * Math.PI / 16, argb(0.42 * f, hsv(i / 16.0 + el / 4000.0, 0.7, 1.0)), 3);

        int S = 60;
        double z = PaymentHud.easeOutBack(clamp01(el / 800.0));
        int HS = (int) (S * Math.max(0.05, z));
        int R = (int) (HS * 0.78);
        ellipse(g.ctx, cx, cy, R + 5, R + 5, argb(0.25 * f, GOLD));
        ellipse(g.ctx, cx, cy, R, R, argb(0.6 * f, 0xE0B000));
        head(g, cx - HS / 2, cy - HS / 2, HS);
        crown(g, cx, cy - HS / 2, HS);

        int ty = (int) (g.h * 0.105);
        text(g, "\u2605\u2605\u2605 LEGENDARY \u2605\u2605\u2605", cx, ty, 1.5f, hsv(el / 600.0, 0.6, 1.0), f);
        int by = cy + R + 8;
        double pop = PaymentHud.easeOutBack(clamp01((el - 400) / 500.0));
        text(g, Stats.fmt(g.n.amount), cx, by, (float) (3.6 * Math.max(0.01, pop)), GOLD, f);
        text(g, g.n.name + " IS A LEGEND", cx, by + 34, 1.5f, WHITE, f);
        text(g, "LARGEST DONATION IN DONUT HISTORY", cx, by + 50, 0.9f, GOLD, f);

        Random r = new Random(g.n.start ^ 0xF1AEL);
        for (int i = 0; i < 7; i++) {
            long at = 500 + i * 900L;
            int fx = (int) (g.w * (0.12 + r.nextDouble() * 0.76)), fy = (int) (g.h * (0.15 + r.nextDouble() * 0.40));
            int col = hsv(r.nextDouble(), 0.8, 1.0);
            PaymentHud.cue(g.n, 2 + i, el, at, "minecraft:entity.firework_rocket.blast", 0.6f, 0.8f + (float) r.nextDouble() * 0.5f);
            burst(g, fx, fy, 30, at, 85, new int[]{col, WHITE, GOLD}, 0xF00L + i);
        }
        confetti(g, 70, 600, RAINBOW);
    }

    // ================================================================ $1M+  LUCKY SLOTS (777)
    private static final int[] REEL_STOP = {1500, 2050, 2600};

    private static void slots(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx;
        fillA(g, 0, 0, g.w, g.h, 0.45 * f, 0x000000);
        int cw = 156, ch = 108, x0 = cx - cw / 2, y0 = (int) (g.h * 0.14);
        double slide = Math.min(PaymentHud.ease(clamp01(el / 400.0)), PaymentHud.ease(clamp01((g.dur - el) / 400.0)));
        y0 -= (int) ((1 - slide) * (y0 + ch + 10));
        if (el < REEL_STOP[2]) PaymentHud.tick(g.n, el, 110, "minecraft:block.note_block.hat", 0.5f, 1.5f);
        for (int i = 0; i < 3; i++)
            PaymentHud.cue(g.n, i, el, REEL_STOP[i], "minecraft:block.stone_button.click_on", 0.9f, 0.9f + i * 0.2f);
        PaymentHud.cue(g.n, 3, el, REEL_STOP[2] + 80, "minecraft:ui.toast.challenge_complete", 1.0f, 1.0f);
        PaymentHud.cue(g.n, 4, el, REEL_STOP[2] + 150, "minecraft:entity.player.levelup", 0.9f, 1.2f);

        g.ctx.fill(x0 - 3, y0 - 3, x0 + cw + 3, y0 + ch + 3, 0xFF3A0A0A);
        g.ctx.fill(x0, y0, x0 + cw, y0 + ch, 0xFF9E1B1B);
        g.ctx.fillGradient(x0, y0, x0 + cw, y0 + 18, 0xFFD42B2B, 0xFF9E1B1B);
        for (int i = 0; i < 15; i++) {
            int bx = x0 + 5 + i * 10;
            int col = ((i + el / 120) % 2 == 0) ? 0xFFFFE680 : 0xFF6A5A10;
            g.ctx.fill(bx, y0 + 3, bx + 4, y0 + 6, col);
            g.ctx.fill(bx, y0 + ch - 6, bx + 4, y0 + ch - 3, col);
        }
        text(g, "\u2605 LUCKY SLOTS \u2605", cx, y0 + 9, 0.9f, GOLD, 1);

        int rw = 40, rh = 50, rgap = 6, rx0 = cx - (3 * rw + 2 * rgap) / 2, ry0 = y0 + 24;
        boolean done = el >= REEL_STOP[2];
        for (int i = 0; i < 3; i++) {
            int rx = rx0 + i * (rw + rgap);
            g.ctx.fill(rx - 2, ry0 - 2, rx + rw + 2, ry0 + rh + 2, done && (el / 130) % 2 == 0 ? 0xFFFFE680 : 0xFF2A2A2A);
            g.ctx.fill(rx, ry0, rx + rw, ry0 + rh, 0xFFF6F2E8);
            double pos = PaymentHud.ease(clamp01(el / (double) REEL_STOP[i])) * (24 + i * 5);
            int k0 = (int) Math.floor(pos);
            g.ctx.enableScissor(rx, ry0, rx + rw, ry0 + rh);
            for (int k = k0 - 1; k <= k0 + 2; k++)
                symbol(g, Math.floorMod(k, 5), rx + rw / 2, ry0 + rh / 2 + (int) Math.round((pos - k) * 36));
            g.ctx.disableScissor();
            g.ctx.fillGradient(rx, ry0, rx + rw, ry0 + 9, 0x66000000, 0x00000000);
            g.ctx.fillGradient(rx, ry0 + rh - 9, rx + rw, ry0 + rh, 0x00000000, 0x66000000);
        }
        // lever
        double pull = Math.sin(Math.PI * clamp01((el - 150) / 700.0));
        g.ctx.fill(x0 + cw + 3, y0 + 26, x0 + cw + 7, y0 + 78, 0xFF8A8A8A);
        ellipse(g.ctx, x0 + cw + 5, y0 + 26 + (int) (pull * 44), 5, 5, 0xFFE02020);

        if (done) {
            double pop = PaymentHud.easeOutBack(clamp01((el - REEL_STOP[2]) / 350.0));
            boolean fl = (el / 140) % 2 == 0;
            text(g, "7 7 7  MEGA WIN", cx, y0 + ch + 8, (float) (1.6 * Math.max(0.01, pop)), fl ? GOLD : WHITE, f);
            text(g, "+" + Stats.fmt(g.n.amount) + " from " + g.n.name, cx, y0 + ch + 28, 1.2f, GREEN, f);
            coins(g, 40, REEL_STOP[2]);
            confetti(g, 50, REEL_STOP[2], GOLDS);
        } else {
            text(g, g.n.name + " pulls the lever...", cx, y0 + ch + 8, 1.0f, WHITE, f);
        }
    }

    private static void symbol(G g, int id, int cx, int cy) {
        DrawContext c = g.ctx;
        switch (id) {
            case 0 -> {                                         // cherries
                c.fill(cx - 1, cy - 11, cx + 1, cy, 0xFF2E7D32);
                c.fill(cx, cy - 11, cx + 8, cy - 9, 0xFF2E7D32);
                ellipse(c, cx - 6, cy + 4, 6, 6, 0xFFD6192B);
                ellipse(c, cx + 6, cy + 5, 6, 6, 0xFFD6192B);
                c.fill(cx - 8, cy + 1, cx - 6, cy + 3, 0xAAFFFFFF);
            }
            case 1 -> {                                         // lemon
                ellipse(c, cx, cy, 11, 8, 0xFFF2D31B);
                ellipse(c, cx - 3, cy - 2, 4, 2, 0xAAFFFFFF);
            }
            case 2 -> {                                         // BAR
                c.fill(cx - 15, cy - 7, cx + 15, cy + 7, 0xFF111111);
                c.fill(cx - 14, cy - 6, cx + 14, cy + 6, 0xFFFFFFFF);
                c.drawText(g.tr, "BAR", cx - g.tr.getWidth("BAR") / 2, cy - 4, 0xFF111111, false);
            }
            case 3 -> {                                         // diamond
                for (int dy = -12; dy <= 12; dy++) {
                    int hw = (int) (11 * (1 - Math.abs(dy) / 12.0));
                    if (hw <= 0) continue;
                    c.fill(cx - hw, cy + dy, cx, cy + dy + 1, 0xFF6FE8FF);
                    c.fill(cx, cy + dy, cx + hw, cy + dy + 1, 0xFF2BB4D6);
                }
            }
            default -> outlined(g, "7", cx, cy - 12, 3f, 0xD6192B, 0xFFFFFF, 1);
        }
    }

    // ================================================================ $10M+  STONKS
    private static void stonks(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.note_block.chime", 1.0f, 1.0f);
        PaymentHud.cue(g.n, 1, el, 3100, "minecraft:ui.toast.challenge_complete", 0.9f, 1.2f);
        PaymentHud.cue(g.n, 2, el, 3100, "minecraft:entity.firework_rocket.launch", 0.7f, 1.1f);
        if (el < 3100) PaymentHud.tick(g.n, el, 130, "minecraft:block.note_block.bit", 0.45f, (float) (0.8 + el / 2500.0));
        fillA(g, 0, 0, g.w, g.h, 0.55 * f, 0x000000);
        int pw = Math.min(g.w - 20, 230), ph = 104, px = cx - pw / 2, py = (int) (g.h * 0.14);
        double slide = Math.min(PaymentHud.ease(clamp01(el / 350.0)), PaymentHud.ease(clamp01((g.dur - el) / 350.0)));
        py -= (int) ((1 - slide) * (py + ph + 10));
        g.ctx.fill(px - 1, py - 1, px + pw + 1, py + ph + 1, 0xFF1E7A3A);
        g.ctx.fill(px, py, px + pw, py + ph, 0xF00A1220);
        for (int i = 1; i < 5; i++) g.ctx.fill(px + 4, py + 14 + i * 16, px + pw - 4, py + 15 + i * 16, 0x22FFFFFF);
        for (int i = 1; i < 8; i++) g.ctx.fill(px + i * pw / 8, py + 18, px + i * pw / 8 + 1, py + ph - 4, 0x16FFFFFF);

        Random r = new Random(g.n.start ^ 0x570C5L);
        final int N = 22;
        double[] v = new double[N];
        for (int i = 0; i < N; i++) v[i] = Math.pow(i / (N - 1.0), 1.7) * 0.82 + 0.06 + (i == 0 ? 0 : (r.nextDouble() - 0.5) * 0.09);
        v[N - 1] = 0.95;
        int ax = px + 10, aw = pw - 20, ay = py + 20, ah = ph - 30;
        double prog = PaymentHud.ease(clamp01((el - 300) / 2800.0)) * (N - 1);
        for (int i = 0; i < N - 1 && i < prog; i++) {
            double fr = Math.min(1, prog - i);
            double x1 = ax + i * aw / (N - 1.0), y1 = ay + ah - v[i] * ah;
            double x2 = ax + (i + fr) * aw / (N - 1.0), y2 = ay + ah - (v[i] + (v[i + 1] - v[i]) * fr) * ah;
            boolean up = v[i + 1] >= v[i];
            int cc = up ? 0xFF22C55E : 0xFFEF4444;
            int mid = (int) ((y1 + y2) / 2), bh = (int) Math.max(2, Math.abs(v[i + 1] - v[i]) * ah);
            g.ctx.fill((int) x1 - 1, mid - bh / 2 - 3, (int) x1, mid + bh / 2 + 3, cc);
            g.ctx.fill((int) x1 - 2, mid - bh / 2, (int) x1 + 1, mid + bh / 2 + 1, cc);
            line(g, x1, y1, x2, y2, 2, 0xFF22EE66);
            if (i + 1 >= prog) ellipse(g.ctx, (int) x2, (int) y2, 3, 3, 0xFFFFFFFF);
        }
        head(g, px + 4, py + 3, 12);
        g.ctx.drawText(g.tr, "STONKS", px + 20, py + 6, 0xFF22EE66, true);
        String pct = "+" + (int) (prog / (N - 1) * 4200) + "%";
        g.ctx.drawText(g.tr, pct, px + pw - 6 - g.tr.getWidth(pct), py + 6, 0xFF22EE66, true);

        double pop = PaymentHud.easeOutBack(clamp01((el - 600) / 400.0));
        outlined(g, "+" + Stats.fmt(g.n.amount), cx, py + ph + 10, (float) (2.6 * Math.max(0.01, pop)), 0x22EE66, 0x052010, f);
        text(g, "from " + g.n.name, cx, py + ph + 34, 1.1f, WHITE, f);
        if (el > 3100) {
            text(g, "TO THE MOON", cx, py + ph + 48, 1.0f, GOLD, f);
            confetti(g, 40, 3100, new int[]{GREEN, GOLD, WHITE});
        }
    }

    // ================================================================ $50M+  LIGHTNING
    private static void lightning(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.46);
        fillA(g, 0, 0, g.w, g.h, 0.64 * f, 0x050810);
        g.ctx.fillGradient(0, 0, g.w, g.h / 3, argb(0.7 * f, 0x181C28), 0);
        long[] strikes = {500, 1150, 1800};
        int HS = 34;
        boolean hit = el > 500;
        int hx = cx - HS / 2 + (hit && el < 2600 ? (int) Math.round(Math.sin(el / 20.0) * 1.5) : 0), hy = cy - HS / 2;
        head(g, hx, hy, HS);
        if (hit) fillA(g, hx, hy, hx + HS, hy + HS, (0.18 + 0.12 * Math.sin(el / 70.0)) * f, 0xFFFF55);
        for (int i = 0; i < 3; i++) {
            PaymentHud.cue(g.n, i, el, strikes[i], "minecraft:entity.lightning_bolt.thunder", 0.9f, 0.8f + i * 0.15f);
            PaymentHud.cue(g.n, 3 + i, el, strikes[i], "minecraft:entity.lightning_bolt.impact", 0.7f, 1.0f);
            long t = el - strikes[i];
            if (t < 0 || t > 280) continue;
            double a = 1 - t / 280.0;
            fillA(g, 0, 0, g.w, g.h, 0.55 * a, 0xFFFFFF);
            bolt(g, cx, hy, i, a);
        }
        if (hit) {                                           // electric aura
            Random r = new Random((el / 70) * 31 + g.n.start);
            for (int k = 0; k < 7; k++) {
                double ang = r.nextDouble() * 6.28, r0 = HS * 0.62, r1 = HS * (0.85 + r.nextDouble() * 0.4);
                line(g, cx + Math.cos(ang) * r0, cy + Math.sin(ang) * r0, cx + Math.cos(ang + 0.2) * r1, cy + Math.sin(ang + 0.2) * r1, 1, argb(0.9 * f, 0x9AE6FF));
            }
        }
        if (el > 700) text(g, "\u26A1 POWER SURGE \u26A1", cx, (int) (g.h * 0.14), 1.5f, (el / 150) % 2 == 0 ? 0xFFF04A : WHITE, f);
        if (el > 1800) {
            double pop = PaymentHud.easeOutBack(clamp01((el - 1800) / 400.0));
            outlined(g, Stats.fmt(g.n.amount), cx, cy + HS / 2 + 14, (float) (2.8 * Math.max(0.01, pop)), 0xFFF04A, 0x1A1A00, f);
            text(g, "from " + g.n.name, cx, cy + HS / 2 + 42, 1.2f, WHITE, f);
        }
    }

    private static void bolt(G g, int tx, int ty, int seed, double a) {
        Random r = new Random(seed * 977L + g.n.start);
        double x = tx + (r.nextDouble() - 0.5) * 80, y = 0;
        double[] px = new double[12], py = new double[12];
        for (int i = 0; i < 12; i++) {
            double k = i / 11.0;
            px[i] = x + (tx - x) * k + (i == 11 ? 0 : (r.nextDouble() - 0.5) * 22);
            py[i] = y + (ty - y) * k;
        }
        for (int i = 0; i < 11; i++) {
            line(g, px[i], py[i], px[i + 1], py[i + 1], 6, argb(0.28 * a, 0x88CCFF));
            line(g, px[i], py[i], px[i + 1], py[i + 1], 2, argb(a, 0xFFFFFF));
        }
        double bx = px[4], by = py[4];
        for (int i = 0; i < 5; i++) {                       // side branch
            double nx = bx + 9 + r.nextDouble() * 8, ny = by + 12 + r.nextDouble() * 8;
            line(g, bx, by, nx, ny, 1, argb(a, 0xDDF2FF));
            bx = nx; by = ny;
        }
    }

    // ================================================================ $50M+  DIAMOND SHATTER
    private static void diamond(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.42);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.amethyst_block.chime", 1.0f, 1.2f);
        PaymentHud.cue(g.n, 1, el, 1500, "minecraft:block.glass.break", 1.0f, 0.9f);
        PaymentHud.cue(g.n, 2, el, 1600, "minecraft:entity.player.levelup", 0.9f, 1.3f);
        g.ctx.fillGradient(0, 0, g.w, g.h, argb(0.78 * f, 0x04141C), argb(0.78 * f, 0x0A2F3C));
        stars(g, 50, f);
        int R = 34;
        if (el < 1500) {
            double fall = bounce(clamp01(el / 900.0));
            int gy = (int) (-R + (cy + R) * fall);
            ellipse(g.ctx, cx, gy, R + 12, R + 12, argb(0.12 * f, 0x66E8FF));
            gem(g, cx, gy, R);
            for (int i = 0; i < 4; i++) {                    // glints
                double ph = (el / 500.0 + i * 0.25) % 1.0;
                int sx = cx + (int) (Math.cos(i * 1.7) * R * 0.7), sy = gy + (int) (Math.sin(i * 2.3) * R * 0.5);
                int s = (int) (1 + 5 * Math.sin(ph * Math.PI));
                g.ctx.fill(sx - s, sy, sx + s + 1, sy + 1, 0xFFFFFFFF);
                g.ctx.fill(sx, sy - s, sx + 1, sy + s + 1, 0xFFFFFFFF);
            }
        } else {
            long t = el - 1500;
            if (t < 400) fillA(g, 0, 0, g.w, g.h, (1 - t / 400.0) * 0.85, 0xE6FBFF);
            Random r = new Random(g.n.start ^ 0xD1A0L);
            for (int i = 0; i < 46; i++) {
                double ang = r.nextDouble() * 6.28, sp = 60 + r.nextDouble() * 150, rs = (r.nextDouble() - 0.5) * 14, sz = 3 + r.nextInt(5);
                double s = t / 1000.0;
                if (s > 1.6) continue;
                double x = cx + Math.cos(ang) * sp * s, y = cy + Math.sin(ang) * sp * s + 140 * s * s;
                int col = r.nextBoolean() ? 0xFF7BEBFF : 0xFF2BB4D6;
                rotRect(g, x, y, sz, sz * 0.6, rs * s, argb(Math.min(1, (1.6 - s) * 2) * f, col));
            }
            double hp = PaymentHud.easeOutBack(clamp01((t - 150) / 500.0));
            int HS = (int) (42 * Math.max(0.01, hp));
            ring(g.ctx, cx, cy, HS * 0.8, 24, t / 400.0, 2, argb(0.8 * f, 0x9AF0FF));
            head(g, cx - HS / 2, cy - HS / 2, HS);
            outlined(g, "DIAMOND HANDS", cx, cy + 30, 1.8f, 0x7BEBFF, 0x05303C, f);
            text(g, Stats.fmt(g.n.amount) + " from " + g.n.name, cx, cy + 50, 1.3f, WHITE, f);
            sparkles(g, cx, cy, 30);
        }
    }

    private static void gem(G g, int cx, int cy, int R) {
        for (int dy = (int) (-R * 0.7); dy <= R; dy++) {
            double hw = dy <= 0 ? R * (0.62 + 0.38 * (dy + R * 0.7) / (R * 0.7)) : R * (1 - dy / (double) R);
            int w = (int) hw;
            if (w <= 0) continue;
            boolean crown = dy < 0;
            g.ctx.fill(cx - w, cy + dy, cx, cy + dy + 1, crown ? 0xFF9AF2FF : 0xFF6FE8FF);
            g.ctx.fill(cx, cy + dy, cx + w, cy + dy + 1, crown ? 0xFF3FC6E6 : 0xFF2BB4D6);
        }
        g.ctx.fill(cx - R, cy, cx + R, cy + 1, 0x99FFFFFF);
        g.ctx.fill(cx - (int) (R * 0.5), (int) (cy - R * 0.7), cx + (int) (R * 0.5), (int) (cy - R * 0.7) + 2, 0xFFE6FBFF);
        line(g, cx - R * 0.5, cy - R * 0.7, cx, cy + R * 0.9, 1, 0x66FFFFFF);
        line(g, cx + R * 0.5, cy - R * 0.7, cx, cy + R * 0.9, 1, 0x44FFFFFF);
    }

    // ================================================================ $100M+  VAULT CRACKED
    private static void vault(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.40), R = 46;
        if (el < 1500) PaymentHud.tick(g.n, el, 220, "minecraft:block.chain.hit", 0.8f, (float) (0.7 + el / 1500.0));
        PaymentHud.cue(g.n, 0, el, 1500, "minecraft:block.iron_door.open", 1.0f, 0.6f);
        PaymentHud.cue(g.n, 1, el, 1700, "minecraft:block.chest.open", 0.9f, 0.9f);
        PaymentHud.cue(g.n, 2, el, 2000, "minecraft:entity.player.levelup", 0.9f, 1.0f);
        fillA(g, 0, 0, g.w, g.h, 0.64 * f, 0x040404);
        double op = PaymentHud.ease(clamp01((el - 1500) / 900.0));
        ellipse(g.ctx, cx, cy, R + 8, R + 8, 0xFF2A2F36);
        ellipse(g.ctx, cx, cy, R + 4, R + 4, 0xFF555B66);
        ellipse(g.ctx, cx, cy, R, R, 0xFF1A1305);
        if (op > 0) {
            ellipse(g.ctx, cx, cy, R - 2, R - 2, argb(op * f, 0xFFB020));
            ellipse(g.ctx, cx, cy, R * 0.6, R * 0.6, argb(op * f, 0xFFE070));
            rays(g, cx, cy, 0, Math.max(g.w, g.h), 14, el / 2000.0, argb(0.22 * op * f, 0xFFE070), 3);
            burst(g, cx, cy, 44, 1500, 120, GOLDS, 0x7A17L);
            confetti(g, 40, 1700, GOLDS);
        }
        if (op < 1) {                                        // the door swinging open on the left hinge
            double sx = 1 - op * 0.88;
            int dcx = (int) (cx - op * R * 0.95);
            double rx = R * sx;
            ellipse(g.ctx, dcx, cy, rx + 3, R + 3, 0xFFB8C0CC);
            ellipse(g.ctx, dcx, cy, rx, R, 0xFF8E96A3);
            for (int i = 0; i < 12; i++) {
                double a = i * Math.PI / 6;
                int rx2 = (int) (dcx + Math.cos(a) * rx * 0.82), ry2 = (int) (cy + Math.sin(a) * R * 0.82);
                g.ctx.fill(rx2 - 1, ry2 - 1, rx2 + 2, ry2 + 2, 0xFF555B66);
            }
            if (rx > 8) {
                double spin = el < 1500 ? Math.pow(el / 1500.0, 2) * 12 : 12;
                for (int k = 0; k < 4; k++) {
                    double a = spin + k * Math.PI / 2;
                    double ex = dcx + Math.cos(a) * 0.55 * rx, ey = cy + Math.sin(a) * 0.55 * R;
                    line(g, dcx, cy, ex, ey, 3, 0xFFD7DCE3);
                    ellipse(g.ctx, (int) ex, (int) ey, Math.max(1, 3 * sx), 3, 0xFFEEF1F5);
                }
                ellipse(g.ctx, dcx, cy, Math.max(2, 8 * sx), 8, 0xFF4A505A);
            }
        }
        if (el > 1900) {
            double hp = PaymentHud.ease(clamp01((el - 1900) / 800.0));
            int HS = (int) (14 + 22 * hp);
            head(g, cx - HS / 2, (int) (cy - HS / 2 - hp * (R + 14)), HS);
        }
        if (el < 1500) {
            String dots = ".".repeat((int) ((el / 250) % 4));
            text(g, "CRACKING VAULT" + dots, cx, cy + R + 14, 1.2f, GRAY, f);
        } else {
            double pop = PaymentHud.easeOutBack(clamp01((el - 1500) / 450.0));
            text(g, "VAULT CRACKED", cx, cy + R + 14, (float) (1.7 * Math.max(0.01, pop)), GOLD, f);
            text(g, Stats.fmt(g.n.amount) + " from " + g.n.name, cx, cy + R + 36, 1.3f, WHITE, f);
        }
    }

    // ================================================================ $500M+  METEOR IMPACT
    private static void meteor(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.55);
        final long IMPACT = 1500;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.firework_rocket.launch", 0.9f, 0.5f);
        PaymentHud.cue(g.n, 1, el, IMPACT, "minecraft:entity.generic.explode", 1.0f, 0.6f);
        PaymentHud.cue(g.n, 2, el, IMPACT + 100, "minecraft:entity.dragon_fireball.explode", 0.8f, 0.7f);
        PaymentHud.cue(g.n, 3, el, IMPACT + 700, "minecraft:ui.toast.challenge_complete", 0.8f, 0.9f);
        fillA(g, 0, 0, g.w, g.h, 0.62 * f, 0x050308);
        stars(g, 60, f);
        if (el < IMPACT) {
            double p = el / (double) IMPACT;
            p = p * p;
            double sx = -30, sy = -30;
            Random r = new Random(el / 30);
            for (int i = 0; i < 40; i++) {
                double tp = Math.max(0, p - i * 0.012);
                double tx = sx + (cx - sx) * tp + (r.nextDouble() - 0.5) * 5, ty = sy + (cy - sy) * tp + (r.nextDouble() - 0.5) * 5;
                int s = (int) Math.max(1, 9 - i * 0.2);
                g.ctx.fill((int) tx - s / 2, (int) ty - s / 2, (int) tx + s / 2 + 1, (int) ty + s / 2 + 1, argb((1 - i / 40.0) * 0.85, hsv(0.02 + 0.07 * (i / 40.0), 1, 1)));
            }
            int mx = (int) (sx + (cx - sx) * p), my = (int) (sy + (cy - sy) * p);
            ellipse(g.ctx, mx, my, 13, 13, 0xFFFF7A18);
            ellipse(g.ctx, mx, my, 10, 10, 0xFFFFC857);
            head(g, mx - 6, my - 6, 12);
        } else {
            long t = el - IMPACT;
            if (t < 450) fillA(g, 0, 0, g.w, g.h, (1 - t / 450.0) * 0.9, 0xFFE9C0);
            double gr = Math.min(1, t / 600.0);
            ellipse(g.ctx, cx, cy, 44 * gr, 15 * gr, argb(0.55 * f, 0xFF4A00));
            ellipse(g.ctx, cx, cy, 28 * gr, 9 * gr, argb(0.85 * f, 0xFFB000));
            for (int k = 0; k < 3; k++) {
                double tt = (t - k * 180) / 1100.0;
                if (tt < 0 || tt > 1) continue;
                ring(g.ctx, cx, cy, tt * Math.max(g.w, g.h) * 0.6, (int) (40 + tt * 80), 0, 2, argb((1 - tt) * f, k == 0 ? WHITE : ORANGE));
            }
            Random r = new Random(g.n.start ^ 0xC4ACL);
            double grow = PaymentHud.ease(clamp01(t / 500.0));
            for (int i = 0; i < 12; i++) {
                double ang = r.nextDouble() * 6.28, len = (30 + r.nextDouble() * 70) * grow;
                line(g, cx, cy, cx + Math.cos(ang) * len, cy + Math.sin(ang) * len * 0.55, 1, argb(0.9 * f, 0x1A0A00));
            }
            burst(g, cx, cy, 34, IMPACT, 140, new int[]{ORANGE, GOLD, 0xFF5522, WHITE}, 0xDEB5L);
            int by = (int) (Math.sin(t / 200.0) * 2);
            head(g, cx - 12, cy - 38 + by, 24);
            if (t > 350) {
                double pop = PaymentHud.easeOutBack(clamp01((t - 350) / 400.0));
                outlined(g, "IMPACT", cx, (int) (g.h * 0.12), (float) (2.4 * Math.max(0.01, pop)), 0xFF7A18, 0x2A0A00, f);
                text(g, Stats.fmt(g.n.amount) + " from " + g.n.name, cx, (int) (g.h * 0.12) + 28, 1.4f, WHITE, f);
            }
        }
    }

    // ================================================================ $1B+  UFO ABDUCTION
    private static void ufo(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, groundY = (int) (g.h * 0.74);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:entity.evoker.prepare_summon", 0.8f, 0.7f);
        if (el < 3600) PaymentHud.tick(g.n, el, 1100, "minecraft:block.beacon.ambient", 0.7f, 1.3f);
        PaymentHud.cue(g.n, 1, el, 3600, "minecraft:entity.player.levelup", 0.9f, 1.4f);
        PaymentHud.cue(g.n, 2, el, 3600, "minecraft:ui.toast.challenge_complete", 0.8f, 1.2f);
        g.ctx.fillGradient(0, 0, g.w, g.h, argb(0.80 * f, 0x060A1C), argb(0.80 * f, 0x0D1F2A));
        stars(g, 80, f);
        g.ctx.fillGradient(0, groundY, g.w, g.h, argb(0.92 * f, 0x070B07), argb(0.92 * f, 0x000000));

        double enter = PaymentHud.ease(clamp01(el / 1000.0));
        int ux = (int) (g.w + 40 - (g.w + 40 - cx) * enter);
        int uy = (int) (g.h * 0.27 + Math.sin(el / 420.0) * 3);
        boolean beam = el > 900 && el < 4300;
        if (beam) {
            double bp = clamp01((el - 900) / 400.0) * clamp01((4300 - el) / 300.0);
            int top = uy + 8;
            for (int y = top; y < groundY; y++) {
                int hw = 8 + (int) ((y - top) * 26.0 / Math.max(1, groundY - top));
                boolean band = ((y - (int) (el / 6)) % 16 + 16) % 16 < 3;
                g.ctx.fill(ux - hw, y, ux + hw, y + 1, argb((band ? 0.34 : 0.20) * bp * f, 0xB6FFB6));
            }
            double lp = PaymentHud.ease(clamp01((el - 1400) / 2200.0));
            int HS = (int) (24 - 12 * lp);
            int hx = ux - HS / 2 + (int) (Math.sin(el / 160.0) * 3 * (1 - lp * 0.5));
            int hy = (int) (groundY - 18 - lp * (groundY - 18 - (uy + 12)));
            if (lp < 1) head(g, hx, hy, HS);
        }
        if (el > 3600 && el < 3900) fillA(g, 0, 0, g.w, g.h, (1 - (el - 3600) / 300.0) * 0.4, 0xB6FFB6);
        ellipse(g.ctx, ux, uy - 6, 14, 10, argb(0.55, 0x9AF0FF));
        ellipse(g.ctx, ux, uy - 7, 5, 5, 0xFF55FF55);
        g.ctx.fill(ux - 3, uy - 8, ux - 1, uy - 7, 0xFF000000);
        g.ctx.fill(ux + 1, uy - 8, ux + 3, uy - 7, 0xFF000000);
        ellipse(g.ctx, ux, uy, 34, 9, 0xFF9AA5B1);
        ellipse(g.ctx, ux, uy + 3, 30, 6, 0xFF6F7A86);
        for (int i = 0; i < 5; i++)
            g.ctx.fill(ux - 24 + i * 12, uy + 2, ux - 20 + i * 12, uy + 5, 0xFF000000 | hsv(i / 5.0 + el / 500.0, 0.8, 1.0));
        text(g, "\u2605 OUT OF THIS WORLD \u2605", cx, (int) (g.h * 0.105), 1.4f, GREEN, f);
        if (el > 3600) {
            double pop = PaymentHud.easeOutBack(clamp01((el - 3600) / 400.0));
            text(g, Stats.fmt(g.n.amount), cx, (int) (g.h * 0.80), (float) (3.2 * Math.max(0.01, pop)), GOLD, f);
            text(g, g.n.name + " was abducted with the cash", cx, (int) (g.h * 0.80) + 30, 1.1f, WHITE, f);
        } else if (el > 900) {
            text(g, "abduction in progress...", cx, (int) (g.h * 0.80), 1.1f, GRAY, f);
        }
    }

    // ================================================================ $10B+  PORTAL
    private static void portal(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.45);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.portal.trigger", 0.8f, 1.0f);
        if (el < 5000) PaymentHud.tick(g.n, el, 1800, "minecraft:block.portal.ambient", 0.7f, 1.0f);
        PaymentHud.cue(g.n, 1, el, 2200, "minecraft:entity.enderman.teleport", 0.9f, 0.8f);
        PaymentHud.cue(g.n, 2, el, 2900, "minecraft:entity.player.levelup", 0.9f, 0.9f);
        fillA(g, 0, 0, g.w, g.h, 0.74 * f, 0x0A0414);
        double open = PaymentHud.ease(clamp01(el / 1400.0));
        Random gr = new Random((el / 90) ^ g.n.start);
        for (int i = 0; i < 5; i++)
            if (gr.nextInt(3) == 0) {
                int y = gr.nextInt(Math.max(1, g.h));
                fillA(g, 0, y, g.w, y + 1 + gr.nextInt(2), 0.10 * f, 0xFF55FF);
            }
        final int rings = 14;
        for (int k = 0; k < rings; k++) {
            double r = (6 + k * 5.2) * open;
            int dots = Math.max(8, (int) (r * 0.85));
            double dir = k % 2 == 0 ? 1 : -1;
            ring(g.ctx, cx, cy, r, dots, dir * el / (500.0 + k * 40), 2,
                argb(f * (0.4 + 0.6 * (1 - k / (double) rings)), hsv(0.78 + 0.12 * (k / (double) rings), 0.7, 1.0)));
        }
        ellipse(g.ctx, cx, cy, 8 * open, 8 * open, argb(0.55 * f, 0xFFFFFF));
        Random r = new Random(g.n.start ^ 0x9047L);
        for (int i = 0; i < 40; i++) {
            double life = 1600 + r.nextDouble() * 1000, off = r.nextDouble() * 6.28, ph = r.nextDouble() * life;
            double t = ((el + ph) % life) / life, rad = t * 120 * open, ang = off + t * 5;
            int x = (int) (cx + Math.cos(ang) * rad), y = (int) (cy + Math.sin(ang) * rad);
            g.ctx.fill(x, y, x + 2, y + 2, argb((1 - t) * f, hsv(0.8 + t * 0.1, 0.6, 1)));
        }
        if (el > 2200) {
            double hp = PaymentHud.easeOutBack(clamp01((el - 2200) / 700.0));
            int HS = (int) (44 * Math.max(0.01, hp));
            ring(g.ctx, cx, cy - (int) (hp * 26), HS * 0.8, 20, el / 300.0, 2, argb(0.8 * f, 0xFFFFFF));
            head(g, cx - HS / 2, cy - HS / 2 - (int) (hp * 26), HS);
        }
        text(g, "PORTAL OPENED", cx, (int) (g.h * 0.105), 1.6f, (el / 160) % 2 == 0 ? PURPLE : 0xFF55FF, f);
        if (el > 2600) {
            double pop = PaymentHud.easeOutBack(clamp01((el - 2600) / 400.0));
            text(g, Stats.fmt(g.n.amount), cx, cy + 62, (float) (3.0 * Math.max(0.01, pop)), GOLD, f);
            text(g, g.n.name + " came through with the cash", cx, cy + 92, 1.1f, WHITE, f);
        }
    }

    // ================================================================ $50B+  TOTAL ECLIPSE
    private static void eclipse(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.40), R = 34;
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:ambient.cave", 0.8f, 0.8f);
        PaymentHud.cue(g.n, 1, el, 2750, "minecraft:entity.lightning_bolt.thunder", 0.6f, 0.5f);
        PaymentHud.cue(g.n, 2, el, 2800, "minecraft:block.beacon.activate", 0.9f, 0.7f);
        PaymentHud.cue(g.n, 3, el, 3400, "minecraft:entity.player.levelup", 0.9f, 0.8f);
        double dark = clamp01(el / 2800.0);
        fillA(g, 0, 0, g.w, g.h, (0.35 + 0.55 * dark) * f, 0x000008);
        if (dark > 0.7) stars(g, 60, (dark - 0.7) / 0.3 * f);
        boolean total = el >= 2800 && el < 5300;
        double cor = el >= 2800 ? 1 : 0.25 + 0.5 * dark;
        ellipse(g.ctx, cx, cy, R + 14, R + 14, argb(0.12 * f, 0xFFD060));
        ellipse(g.ctx, cx, cy, R + 7, R + 7, argb(0.22 * f, 0xFFD060));
        ellipse(g.ctx, cx, cy, R, R, 0xFFFFD84A);
        rays(g, cx, cy, R, R + 26 + 44 * cor, 36, el / 6000.0, argb(0.34 * cor * f, 0xFFF0C0), 1);
        rays(g, cx, cy, R, R + 14 + 26 * cor, 36, el / 6000.0 + 0.05, argb(0.45 * cor * f, 0xFFFFFF), 2);
        double mp = el < 2800 ? PaymentHud.ease(el / 2800.0) : (el < 5300 ? 1 : 1 + PaymentHud.ease(clamp01((el - 5300) / 1700.0)));
        int mx = (int) (cx - 170 + 170 * mp);
        ellipse(g.ctx, mx, cy, R + 1, R + 1, 0xFF05050A);
        if (el > 2700 && el < 3200) {
            double a = 1 - (el - 2700) / 500.0;
            fillA(g, 0, 0, g.w, g.h, 0.3 * a, 0xFFFFFF);
            ellipse(g.ctx, cx + (int) (R * 0.7), cy - (int) (R * 0.7), 5, 5, argb(a, 0xFFFFFF));
        }
        if (total) ring(g.ctx, cx, cy, R + 3, 70, 0, 1, argb(0.9 * f, 0xFFF6D0));
        if (el > 3000) {
            text(g, "TOTAL ECLIPSE", cx, cy + R + 22, 1.6f, WHITE, f);
            double pop = PaymentHud.easeOutBack(clamp01((el - 3400) / 450.0));
            text(g, Stats.fmt(g.n.amount), cx, cy + R + 44, (float) (3.2 * Math.max(0.01, pop)), GOLD, f);
            text(g, "from " + g.n.name, cx, cy + R + 76, 1.3f, WHITE, f);
        }
    }

    // ================================================================ $100B+  MONEY STORM
    private static void storm(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.42);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:weather.rain", 0.7f, 1.0f);
        PaymentHud.cue(g.n, 1, el, 0, "minecraft:entity.player.levelup", 0.9f, 0.8f);
        PaymentHud.cue(g.n, 2, el, 700, "minecraft:entity.lightning_bolt.thunder", 0.6f, 0.9f);
        g.ctx.fillGradient(0, 0, g.w, g.h, argb(0.80 * f, 0x04180A), argb(0.80 * f, 0x0A3A1A));
        Random r = new Random(g.n.start ^ 0xB177L);
        for (int i = 0; i < 110; i++) {
            double x0 = r.nextDouble() * (g.w + 160), spd = 150 + r.nextDouble() * 170, off = r.nextDouble() * (g.h + 40);
            double ang = r.nextDouble() * 6.28, spin = r.nextDouble() * 8 - 4, sz = 0.7 + r.nextDouble() * 0.8;
            double y = ((el * spd / 1000.0 + off) % (g.h + 40)) - 20;
            double x = ((x0 + y * 0.35 + Math.sin(el / 300.0 + i) * 8) % (g.w + 160)) - 40;
            bill(g, x, y, ang + spin * el / 1000.0, (float) sz);
        }
        if ((el / 900) % 5 == 3 && el % 900 < 120) fillA(g, 0, 0, g.w, g.h, 0.22, 0xFFFFFF);
        if (el > 300) {
            double pop = PaymentHud.easeOutBack(clamp01((el - 300) / 450.0));
            outlined(g, "MAKE IT RAIN", cx, cy - 26, (float) (2.4 * Math.max(0.01, pop)), (el / 200) % 2 == 0 ? 0x55FF55 : GOLD, 0x021008, f);
            outlined(g, Stats.fmt(g.n.amount), cx, cy + 2, (float) (3.8 * Math.max(0.01, pop)), GOLD, 0x2A1A00, f);
            outlined(g, "from " + g.n.name, cx, cy + 44, 1.5f, WHITE, 0x021008, f);
        }
    }

    private static void bill(G g, double x, double y, double rot, float sz) {
        int a = (int) (6 * sz), b = (int) (3 * sz);
        g.ms.pushMatrix();
        g.ms.translate((float) x, (float) y);
        g.ms.rotate((float) rot);
        g.ctx.fill(-a, -b, a, b, 0xFF1F6B2E);
        g.ctx.fill(-a + 1, -b + 1, a - 1, b - 1, 0xFF3E9E52);
        g.ctx.fill(-1, -1, 1, 1, 0xFFC8F0CE);
        g.ms.popMatrix();
    }

    // ================================================================ $1T+  BIG BANG
    private static void bigbang(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx, cy = (int) (g.h * 0.42);
        PaymentHud.cue(g.n, 0, el, 0, "minecraft:block.beacon.activate", 0.6f, 0.5f);
        PaymentHud.cue(g.n, 1, el, 1400, "minecraft:entity.generic.explode", 1.0f, 0.4f);
        PaymentHud.cue(g.n, 2, el, 1400, "minecraft:entity.ender_dragon.growl", 0.8f, 0.6f);
        PaymentHud.cue(g.n, 3, el, 1700, "minecraft:block.end_portal.spawn", 0.7f, 0.7f);
        PaymentHud.cue(g.n, 4, el, 3400, "minecraft:ui.toast.challenge_complete", 1.0f, 0.8f);
        fillA(g, 0, 0, g.w, g.h, 0.96 * Math.min(1, el / 300.0) * f, 0x000000);
        if (el < 1400) {
            double k = el / 1400.0;
            ring(g.ctx, cx, cy, 70 * (1 - k) + 4, 36, el / 90.0, 1, argb(k * f, 0xFFFFFF));
            int jx = (int) (Math.sin(el / 17.0) * 2 * k), jy = (int) (Math.cos(el / 13.0) * 2 * k);
            ellipse(g.ctx, cx + jx, cy + jy, 3 + 4 * k, 3 + 4 * k, 0xFFFFFFFF);
            ellipse(g.ctx, cx + jx, cy + jy, 8 + 16 * k, 8 + 16 * k, argb(0.25 * k, 0xFFFFFF));
            text(g, "...", cx, cy + 30, 1.2f, GRAY, f);
        } else {
            long t = el - 1400;
            if (t < 400) fillA(g, 0, 0, g.w, g.h, 1 - t / 400.0, 0xFFFFFF);
            double e = PaymentHud.ease(clamp01(t / 2600.0));
            double Rm = Math.min(g.w, g.h) * 0.48 * e;
            for (int arm = 0; arm < 4; arm++)
                for (int j = 0; j < 46; j++) {
                    double rr = Rm * j / 46.0, ang = arm * Math.PI / 2 + j * 0.20 + el / 2600.0;
                    int x = (int) (cx + Math.cos(ang) * rr), y = (int) (cy + Math.sin(ang) * rr * 0.62);
                    int sz = j < 12 ? 4 : (j < 30 ? 3 : 2);
                    g.ctx.fill(x, y, x + sz, y + sz, argb((0.55 + 0.45 * (1 - j / 46.0)) * f, hsv(0.62 - 0.5 * j / 46.0, 0.55, 1.0)));
                }
            Random r = new Random(g.n.start ^ 0xBA46L);
            for (int i = 0; i < 150; i++) {
                double rr = Rm * Math.sqrt(r.nextDouble()), ang = r.nextDouble() * 6.28 + el / 3000.0 * (1 - rr / Math.max(1, Rm));
                int x = (int) (cx + Math.cos(ang) * rr), y = (int) (cy + Math.sin(ang) * rr * 0.62);
                g.ctx.fill(x, y, x + 1, y + 1, argb((0.4 + 0.6 * Math.abs(Math.sin(el / 300.0 + i))) * f, WHITE));
            }
            for (int k = 4; k >= 1; k--) ellipse(g.ctx, cx, cy, Rm * 0.25 * k, Rm * 0.25 * k * 0.62, argb(0.07 * f, 0x8FA0FF));
            ellipse(g.ctx, cx, cy, 10 + 8 * e, 6 + 5 * e, argb(0.55 * f, 0xFFF0C0));
            ellipse(g.ctx, cx, cy, 4 + 3 * e, 3 + 2 * e, argb(f, 0xFFFFFF));
            if (t > 700) text(g, "THE BIG BANG", cx, (int) (g.h * 0.105), 1.8f, (el / 200) % 2 == 0 ? WHITE : GOLD, f);
            if (t > 1500) {
                double pop = PaymentHud.easeOutBack(clamp01((t - 1500) / 500.0));
                outlined(g, Stats.fmt(g.n.amount), cx, (int) (g.h * 0.76), (float) (4.2 * Math.max(0.01, pop)), GOLD, 0x2A1A00, f);
                text(g, g.n.name + " created a universe", cx, (int) (g.h * 0.76) + 36, 1.4f, WHITE, f);
            }
        }
    }

    // ================================================================ $100M+  FIREWORKS
    private static void fireworks(G g) {
        long el = g.el; double f = g.fade; int cx = g.cx;
        g.ctx.fillGradient(0, 0, g.w, g.h, argb(0.80 * f, 0x03030F), argb(0.80 * f, 0x0A1030));
        stars(g, 80, f);
        Random sk = new Random(g.n.start ^ 0x5C1FL);
        for (int i = 0; i < g.w / 12 + 1; i++) {
            int bh = 10 + sk.nextInt(34);
            g.ctx.fill(i * 12, g.h - bh, i * 12 + 12, g.h, argb(0.95 * f, 0x05050A));
        }
        Random r = new Random(g.n.start ^ 0xF14E5L);
        final long rise = 650;
        for (int i = 0; i < 9; i++) {
            long launch = 300 + i * 620L;
            int fx = (int) (g.w * (0.12 + r.nextDouble() * 0.76)), fy = (int) (g.h * (0.14 + r.nextDouble() * 0.34));
            int col = hsv(r.nextDouble(), 0.8, 1.0);
            PaymentHud.cue(g.n, i, el, launch, "minecraft:entity.firework_rocket.launch", 0.5f, 1.0f);
            PaymentHud.cue(g.n, 9 + i, el, launch + rise, "minecraft:entity.firework_rocket.blast", 0.6f, 0.8f + (i % 4) * 0.12f);
            long t = el - launch;
            if (t >= 0 && t < rise) {
                double p = PaymentHud.ease(t / (double) rise);
                int y = (int) (g.h - (g.h - fy) * p);
                for (int k = 0; k < 8; k++) g.ctx.fill(fx, y + k * 4, fx + 2, y + k * 4 + 2, argb((1 - k / 8.0) * f, 0xFFE0A0));
            }
            burst(g, fx, fy, 48, launch + rise, 135, new int[]{col, WHITE}, 0xF1E0L + i);
            burst(g, fx, fy, 28, launch + rise + 120, 80, new int[]{GOLD, col}, 0xF1E1L + i);
        }
        if (el > 400) {
            double pop = PaymentHud.easeOutBack(clamp01((el - 400) / 450.0));
            outlined(g, "+" + Stats.fmt(g.n.amount), cx, (int) (g.h * 0.40), (float) (3.2 * Math.max(0.01, pop)), GOLD, 0x1A1000, f);
            outlined(g, "from " + g.n.name, cx, (int) (g.h * 0.40) + 34, 1.4f, WHITE, 0x050510, f);
        }
    }

    // ================================================================ drawing helpers for the new shows
    private static void line(G g, double x1, double y1, double x2, double y2, double thick, int argb) {
        double dx = x2 - x1, dy = y2 - y1, len = Math.hypot(dx, dy);
        if (len < 0.5) return;
        int t = (int) Math.max(1, Math.round(thick));
        g.ms.pushMatrix();
        g.ms.translate((float) x1, (float) y1);
        g.ms.rotate((float) Math.atan2(dy, dx));
        g.ctx.fill(0, -t / 2, (int) Math.ceil(len), t - t / 2, argb);
        g.ms.popMatrix();
    }

    private static void rotRect(G g, double cx, double cy, double w, double h, double rot, int argb) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) cy);
        g.ms.rotate((float) rot);
        g.ctx.fill((int) (-w / 2), (int) (-h / 2), (int) Math.ceil(w / 2), (int) Math.ceil(h / 2), argb);
        g.ms.popMatrix();
    }

    // ================================================================ shared effects
    private static void rocket(G g, int cx, int cy) {
        final int u = 2; double t = g.el;
        int ry = (int) (cy + 50 - (0.00002 * t * t + 0.03 * t));
        int rx = cx + (int) Math.round(Math.sin(t / 120.0) * 1.5);
        if (ry < -40) return;
        int bw = 8 * u, bx = rx - bw / 2;
        DrawContext c = g.ctx;
        c.fill(rx - u, ry - 3 * u, rx + u, ry - 2 * u, 0xFFE8E8E8);
        c.fill(rx - 2 * u, ry - 2 * u, rx + 2 * u, ry - u, 0xFFEFEFEF);
        c.fill(rx - 3 * u, ry - u, rx + 3 * u, ry, 0xFFF4F4F4);
        c.fill(bx, ry, bx + bw, ry + 12 * u, 0xFFF4F4F4);
        c.fill(bx + bw - u, ry, bx + bw, ry + 12 * u, 0xFFCFCFCF);
        c.fill(bx - 3 * u, ry + 8 * u, bx, ry + 12 * u, 0xFFD83A32);
        c.fill(bx - 2 * u, ry + 6 * u, bx, ry + 8 * u, 0xFFD83A32);
        c.fill(bx + bw, ry + 8 * u, bx + bw + 3 * u, ry + 12 * u, 0xFFD83A32);
        c.fill(bx + bw, ry + 6 * u, bx + bw + 2 * u, ry + 8 * u, 0xFFD83A32);
        c.fill(bx + u, ry + u, bx + bw - u, ry + 7 * u, 0xFF7A7A7A);
        PlayerSkinDrawer.draw(c, g.skin, bx + 2 * u, ry + 2 * u, 8);
        boolean flick = (g.el / 70) % 2 == 0;
        c.fill(rx - 2 * u, ry + 12 * u, rx + 2 * u, ry + 14 * u, 0xFFFFC83A);
        c.fill(rx - u, ry + 14 * u, rx + u, ry + (flick ? 17 : 16) * u, 0xFFFF8A1F);
    }

    private static void headBurst(G g, int cx, int cy) {
        Random r = new Random(g.n.start ^ 0x5DEECE66DL);
        for (int i = 0; i < 22; i++) {
            double ang = -Math.PI / 2 + (r.nextDouble() - 0.5) * Math.PI * 1.6;
            double spd = 70 + r.nextDouble() * 150, life = 1200 + r.nextDouble() * 1100, delay = r.nextDouble() * 350;
            double t = g.el - delay;
            if (t < 0 || t > life) continue;
            double s = t / 1000.0;
            int px = (int) (cx + Math.cos(ang) * spd * s), py = (int) (cy + Math.sin(ang) * spd * s + 170 * s * s);
            double k = t / life;
            int size = Math.max(2, (int) Math.round(8 * (1 - Math.max(0, (k - 0.75) / 0.25))));
            PlayerSkinDrawer.draw(g.ctx, g.skin, px - size / 2, py - size / 2, size);
        }
    }

    private static void bubbles(G g) {
        Random r = new Random(g.n.start ^ 0xB0B0L);
        for (int i = 0; i < 16; i++) {
            int bx = (int) (r.nextDouble() * g.w), rad = 4 + r.nextInt(9);
            double spd = 28 + r.nextDouble() * 45, delay = r.nextDouble() * 1500, life = 2200 + r.nextDouble() * 1400;
            double t = g.el - delay;
            if (t < 0 || t > life) continue;
            double k = t / life;
            int by = (int) (g.h * 0.95 - spd * (t / 1000.0));
            int bxw = bx + (int) Math.round(Math.sin(t / 380.0 + i) * 4);
            int a = (int) (0x66 * g.fade * (1 - k * k));
            ellipse(g.ctx, bxw, by, rad, rad, (a << 24) | 0xFFFFFF);
            ellipse(g.ctx, bxw - rad / 3, by - rad / 3, Math.max(1, rad / 3), Math.max(1, rad / 3), (Math.min(255, a + 0x30) << 24) | 0xFFFFFF);
        }
    }

    private static void sparkles(G g, int cx, int cy, int count) {
        Random r = new Random(g.n.start);
        for (int i = 0; i < count; i++) {
            double ang = r.nextDouble() * Math.PI * 2, spd = 40 + r.nextDouble() * 140;
            double life = 900 + r.nextDouble() * 1800, delay = r.nextDouble() * 500;
            double t = g.el - delay;
            int size = 1 + r.nextInt(3);
            boolean gold = r.nextBoolean();
            if (t < 0 || t > life) continue;
            double k = t / life;
            int px = (int) (cx + Math.cos(ang) * spd * (t / 1000.0) * 1.4);
            int py = (int) (cy + Math.sin(ang) * spd * (t / 1000.0) * 1.4 + 60 * k * k);
            g.ctx.fill(px, py, px + size, py + size, argb((1 - k) * g.fade, gold ? GOLD : WHITE));
        }
    }

    private static void glow(G g, double strength) {
        int a = (int) (255 * strength * g.fade);
        g.ctx.fillGradient(0, 0, g.w, g.h / 3, (a << 24) | 0xFFB000, 0x00FFB000);
        g.ctx.fillGradient(0, g.h - g.h / 3, g.w, g.h, 0x00FFB000, (a << 24) | 0xFFB000);
    }

    private static void coins(G g, int count, long startMs) {
        Random r = new Random(g.n.start ^ 0xC01DL);
        for (int i = 0; i < count; i++) {
            double x0 = r.nextDouble() * g.w, spd = 70 + r.nextDouble() * 90, delay = startMs + r.nextDouble() * 1100;
            double rad = 3 + r.nextInt(3), ph = r.nextDouble() * 6.28;
            double t = g.el - delay;
            if (t < 0) continue;
            int y = (int) (-8 + spd * t / 1000.0);
            if (y > g.h + 8) continue;
            int x = (int) (x0 + Math.sin(t / 400.0 + ph) * 6);
            double c = Math.abs(Math.cos(t / 140.0 + ph));
            double rx = Math.max(1, rad * c);
            ellipse(g.ctx, x, y, rx + 1, rad + 1, 0xFFB8860B);
            ellipse(g.ctx, x, y, rx, rad, 0xFFFFD700);
            if (c > 0.45) ellipse(g.ctx, x, y, rx * 0.55, rad * 0.55, 0xFFFFE680);
        }
    }

    private static void coinBody(G g, int cx, int cy, double rx, double ry, double facing) {
        int r = (int) Math.ceil(ry);
        for (int dy = -r; dy <= r; dy++) {
            double t = dy / ry;
            if (Math.abs(t) > 1) continue;
            int hw = (int) Math.round(rx * Math.sqrt(1 - t * t));
            if (hw <= 0) continue;
            int col = t < -0.33 ? 0xFFFFE680 : (t > 0.45 ? 0xFFB88A00 : 0xFFFFD21A);
            g.ctx.fill(cx - hw - 1, cy + dy, cx + hw + 1, cy + dy + 1, 0xFF8A6200);
            g.ctx.fill(cx - hw, cy + dy, cx + hw, cy + dy + 1, col);
        }
        if (facing > 0.5) {                                 // little embossed skull
            int ex = (int) Math.round(7 * facing), e = Math.max(1, (int) Math.round(3 * facing));
            g.ctx.fill(cx - ex - e, cy - 9, cx - ex + e, cy - 5, 0xFF1A1A1A);
            g.ctx.fill(cx + ex - e, cy - 9, cx + ex + e, cy - 5, 0xFF1A1A1A);
            int mw = (int) Math.round(8 * facing);
            g.ctx.fill(cx - mw, cy + 1, cx + mw, cy + 3, 0xFF1A1A1A);
            g.ctx.fill(cx - mw, cy + 6, cx + mw, cy + 8, 0xFF1A1A1A);
        }
    }

    private static void glasses(G g, int hx, int hy, int S, double drop) {
        int ey = hy + (int) (S * 0.42) - (int) ((1 - drop) * S * 1.4);
        int lh = Math.max(3, (int) (S * 0.2)), lw = (int) (S * 0.44);
        int lx1 = hx + (int) (S * 0.04), lx2 = hx + (int) (S * 0.52);
        int k = 0xFF000000, arm = Math.max(1, lh / 4);
        g.ctx.fill(lx1, ey, lx1 + lw, ey + lh, k);
        g.ctx.fill(lx2, ey, lx2 + lw, ey + lh, k);
        g.ctx.fill(lx1 + lw, ey, lx2, ey + Math.max(1, lh / 3), k);
        g.ctx.fill(hx - 2, ey, lx1, ey + arm, k);
        g.ctx.fill(lx2 + lw, ey, hx + S + 2, ey + arm, k);
        g.ctx.fill(lx1 + 2, ey + 1, lx1 + 4, ey + 2, 0xAAFFFFFF);
        g.ctx.fill(lx2 + 2, ey + 1, lx2 + 4, ey + 2, 0xAAFFFFFF);
    }

    private static void crown(G g, int cx, int headTop, int HS) {
        int bw = (int) (HS * 0.7), bx = cx - bw / 2, by = headTop - (int) (HS * 0.10), ph = (int) (HS * 0.22);
        g.ctx.fill(bx, by - 4, bx + bw, by + 3, 0xFFFFD700);
        g.ctx.fill(bx, by - 4, bx + bw, by - 3, 0xFFFFF2A0);
        int pw = Math.max(3, bw / 5);
        for (int i = 0; i < 3; i++) {
            int px = bx + i * (bw - pw) / 2;
            g.ctx.fill(px, by - 4 - ph, px + pw, by - 4, 0xFFFFD700);
            g.ctx.fill(px + pw / 2, by - 6 - ph, px + pw / 2 + 2, by - 4 - ph, 0xFFFFF2A0);
        }
        g.ctx.fill(cx - 2, by - 2, cx + 2, by + 1, 0xFFD83A32);
    }

    private static void laser(G g, int ex, int ey, double ang, double a) {
        g.ms.pushMatrix();
        g.ms.translate((float) ex, (float) ey);
        g.ms.rotate((float) ang);
        int len = Math.max(g.w, g.h) + 100;
        g.ctx.fill(0, -3, len, 3, argb(0.25 * a, PINK));
        g.ctx.fill(0, -1, len, 1, argb(a, PINK));
        g.ctx.fill(0, 0, len, 1, argb(a, WHITE));
        g.ms.popMatrix();
    }

    private static void rays(G g, int cx, int cy, double r0, double r1, int count, double rot, int argb, int thick) {
        for (int i = 0; i < count; i++) {
            g.ms.pushMatrix();
            g.ms.translate((float) cx, (float) cy);
            g.ms.rotate((float) (rot + i * 2 * Math.PI / count));
            g.ctx.fill((int) r0, 0, (int) r1, Math.max(1, thick), argb);
            g.ms.popMatrix();
        }
    }

    private static void ring(DrawContext ctx, int cx, int cy, double r, int dots, double rot, int size, int argb) {
        for (int i = 0; i < dots; i++) {
            double a = rot + i * 2 * Math.PI / dots;
            int x = (int) Math.round(cx + Math.cos(a) * r), y = (int) Math.round(cy + Math.sin(a) * r);
            ctx.fill(x, y, x + size, y + size, argb);
        }
    }

    private static void ellipse(DrawContext ctx, int cx, int cy, double rx, double ry, int argb) {
        int r = (int) Math.ceil(ry);
        for (int dy = -r; dy <= r; dy++) {
            double t = dy / ry;
            if (Math.abs(t) > 1) continue;
            int hw = (int) Math.round(rx * Math.sqrt(1 - t * t));
            if (hw <= 0) continue;
            ctx.fill(cx - hw, cy + dy, cx + hw, cy + dy + 1, argb);
        }
    }

    private static void stars(G g, int count, double alpha) {
        Random r = new Random(g.n.start ^ 0x57A5L);
        for (int i = 0; i < count; i++) {
            int x = r.nextInt(Math.max(1, g.w)), y = r.nextInt(Math.max(1, g.h));
            double ph = r.nextDouble() * 6.28, tw = 0.5 + 0.5 * Math.sin(g.el / 220.0 + ph);
            int sz = r.nextInt(4) == 0 ? 2 : 1;
            g.ctx.fill(x, y, x + sz, y + sz, argb(alpha * tw, WHITE));
        }
    }

    private static void confetti(G g, int count, long startMs, int[] cols) {
        Random r = new Random(g.n.start ^ 0xC0FFEEL);
        for (int i = 0; i < count; i++) {
            double x0 = r.nextDouble() * g.w, vy = 60 + r.nextDouble() * 120;
            double life = 2500 + r.nextDouble() * 1500, delay = startMs + r.nextDouble() * 1200;
            int sz = 2 + r.nextInt(2), col = cols[i % cols.length];
            double t = g.el - delay;
            if (t < 0 || t > life) continue;
            int y = (int) (-10 + vy * t / 1000.0), x = (int) (x0 + Math.sin(t / 300.0 + i) * 14);
            g.ctx.fill(x, y, x + sz, y + sz, argb(Math.min(1, (1 - t / life) * 2) * g.fade, col));
        }
    }

    private static void burst(G g, int cx, int cy, int count, long delay, double spd, int[] cols, long seed) {
        Random r = new Random(seed ^ g.n.start);
        double t = g.el - delay;
        if (t < 0 || t > 1500) return;
        double s = t / 1000.0, k = t / 1500.0;
        for (int i = 0; i < count; i++) {
            double ang = i * 2 * Math.PI / count + r.nextDouble() * 0.3, v = spd * (0.6 + r.nextDouble() * 0.6);
            int x = (int) (cx + Math.cos(ang) * v * s), y = (int) (cy + Math.sin(ang) * v * s + 70 * s * s);
            g.ctx.fill(x, y, x + 2, y + 2, argb(1 - k, cols[i % cols.length]));
        }
    }

    // ================================================================ small helpers
    private static void head(G g, int x, int y, int size) { PlayerSkinDrawer.draw(g.ctx, g.skin, x, y, size); }

    private static void fillA(G g, int x1, int y1, int x2, int y2, double alpha, int rgb) {
        g.ctx.fill(x1, y1, x2, y2, argb(alpha, rgb));
    }

    private static void text(G g, String s, int cx, int y, float sc, int rgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        g.ctx.drawCenteredTextWithShadow(g.tr, Text.literal(s), 0, 0, targb(alpha, rgb));
        g.ms.popMatrix();
    }

    private static void textNS(G g, String s, int cx, int y, float sc, int rgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        g.ctx.drawText(g.tr, s, -g.tr.getWidth(s) / 2, 0, targb(alpha, rgb), false);
        g.ms.popMatrix();
    }

    private static void outlined(G g, String s, int cx, int y, float sc, int rgb, int outRgb, double alpha) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        int x = -g.tr.getWidth(s) / 2, oc = targb(alpha, outRgb);
        int[][] d = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}};
        for (int[] o : d) g.ctx.drawText(g.tr, s, x + o[0], o[1], oc, false);
        g.ctx.drawText(g.tr, s, x, 0, targb(alpha, rgb), false);
        g.ms.popMatrix();
    }

    private static void rainbowText(G g, String s, int cx, int y, float sc, long el) {
        g.ms.pushMatrix();
        g.ms.translate((float) cx, (float) y);
        g.ms.scale(sc, sc);
        int x = -g.tr.getWidth(s) / 2;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            g.ctx.drawText(g.tr, ch, x, 0, targb(g.fade, hsv(el / 700.0 + i * 0.08, 0.85, 1.0)), true);
            x += g.tr.getWidth(ch);
        }
        g.ms.popMatrix();
    }

    private static float fit(G g, String s, float sc, int maxW) {
        int tw = Math.max(1, g.tr.getWidth(s));
        return tw * sc > maxW ? maxW / (float) tw : sc;
    }

    private static double clamp01(double v) { return Math.max(0, Math.min(1, v)); }

    private static double bounce(double t) {
        double n1 = 7.5625, d1 = 2.75;
        if (t < 1 / d1) return n1 * t * t;
        if (t < 2 / d1) { t -= 1.5 / d1; return n1 * t * t + 0.75; }
        if (t < 2.5 / d1) { t -= 2.25 / d1; return n1 * t * t + 0.9375; }
        t -= 2.625 / d1;
        return n1 * t * t + 0.984375;
    }

    /** alpha for text (never 0, vanilla treats 0 as opaque). */
    private static int targb(double alpha, int rgb) { return (PaymentHud.clampA(alpha) << 24) | (rgb & 0xFFFFFF); }

    /** alpha for fills (0 allowed). */
    private static int argb(double alpha, int rgb) { return fillArgb(alpha, rgb); }

    private static int fillArgb(double alpha, int rgb) {
        return (Math.max(0, Math.min(255, (int) (alpha * 255))) << 24) | (rgb & 0xFFFFFF);
    }

    static int hsv(double h, double s, double v) {
        h = ((h % 1) + 1) % 1;
        double c = v * s, x = c * (1 - Math.abs((h * 6) % 2 - 1)), m = v - c, r, gg, b;
        int k = (int) (h * 6) % 6;
        if (k == 0) { r = c; gg = x; b = 0; }
        else if (k == 1) { r = x; gg = c; b = 0; }
        else if (k == 2) { r = 0; gg = c; b = x; }
        else if (k == 3) { r = 0; gg = x; b = c; }
        else if (k == 4) { r = x; gg = 0; b = c; }
        else { r = c; gg = 0; b = x; }
        return (((int) ((r + m) * 255)) << 16) | (((int) ((gg + m) * 255)) << 8) | ((int) ((b + m) * 255));
    }
}
