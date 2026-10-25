package fr.thunderbeacon;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;

import java.util.List;

/**
 * Rendu du rayon de balise : colonne ronde vibrante, noyau sphérique,
 * orbes d'énergie qui montent le long du rayon et petites orbes en hélice.
 */
public final class ThunderBeamRenderer {

    private static final int LIGHT = 0xF000F0;   // pleine luminosité
    private static final int CYL_SIDES = 24;
    private static final int SPH_LAT = 10;
    private static final int SPH_LON = 16;

    // Palette bleu tonnerre
    private static final int[] DEEP   = {30, 80, 255};
    private static final int[] ELEC   = {60, 140, 255};
    private static final int[] BRIGHT = {120, 200, 255};
    private static final int[] WHITE  = {225, 242, 255};

    private ThunderBeamRenderer() {}

    public static void render(BeaconBlockEntity beacon, float tickDelta, MatrixStack ms, VertexConsumerProvider vcp) {
        World world = beacon.getWorld();
        if (world == null) return;

        List<BeaconBlockEntity.BeamSegment> segments = beacon.getBeamSegments();
        int height = 0;
        for (BeaconBlockEntity.BeamSegment s : segments) height += s.getHeight();
        if (height <= 0) return;

        float t = world.getTime() + tickDelta;

        // Vibration : plusieurs sinus désaccordés + scintillement rapide
        float jitter = 1.0f
            + 0.07f * (float) Math.sin(t * 2.1f)
            + 0.04f * (float) Math.sin(t * 7.7f)
            + 0.03f * (float) Math.sin(t * 13.3f);
        float flicker = 0.80f + 0.20f * (float) Math.sin(t * 9.0f + (float) Math.sin(t * 2.3f) * 3.0f);

        VertexConsumer solid = vcp.getBuffer(RenderLayer.getBeaconBeam(BeaconBlockEntityRenderer.BEAM_TEXTURE, false));
        VertexConsumer glow  = vcp.getBuffer(RenderLayer.getBeaconBeam(BeaconBlockEntityRenderer.BEAM_TEXTURE, true));

        ms.push();
        ms.translate(0.5, 0.0, 0.5);
        MatrixStack.Entry e = ms.peek();

        float vs = -t * 0.06f;               // défilement de la texture
        float ve = vs + height * 0.5f;

        // --- Colonne ronde (3 couches) ---
        cylinder(solid, e, 0.10f * jitter, 0f, height, vs, ve, argb(255, WHITE));
        cylinder(glow,  e, 0.20f * jitter, 0f, height, vs * 1.7f, ve * 1.7f, argb((int) (170 * flicker), BRIGHT));
        cylinder(glow,  e, 0.34f * jitter, 0f, height, -vs, -ve, argb((int) (90 * flicker), ELEC));
        cylinder(glow,  e, 0.52f * jitter, 0f, height, vs * 0.5f, ve * 0.5f, argb((int) (45 * flicker), DEEP));

        // --- Noyau sphérique au-dessus de la balise ---
        float coreY = 1.9f;
        float pulse = 1.0f + 0.12f * (float) Math.sin(t * 0.35f) + 0.05f * (float) Math.sin(t * 6.0f);
        sphere(solid, e, 0, coreY, 0, 0.30f * pulse, argb(255, WHITE));
        sphere(glow,  e, 0, coreY, 0, 0.45f * pulse, argb((int) (190 * flicker), BRIGHT));
        sphere(glow,  e, 0, coreY, 0, 0.68f * pulse, argb((int) (95 * flicker), ELEC));
        sphere(glow,  e, 0, coreY, 0, 0.95f * pulse, argb((int) (45 * flicker), DEEP));

        // --- Orbes d'énergie qui montent le long du rayon ---
        final int orbs = 14;
        final float spacing = 9.0f;
        final float range = orbs * spacing;
        for (int i = 0; i < orbs; i++) {
            float y = 2.5f + mod(t * 0.45f + i * spacing, range);
            if (y > height) continue;
            float r = 0.20f + 0.05f * (float) Math.sin(t * 0.5f + i * 1.7f);
            sphere(solid, e, 0, y, 0, r * 0.6f, argb(255, WHITE));
            sphere(glow,  e, 0, y, 0, r * 1.5f, argb((int) (150 * flicker), BRIGHT));
            sphere(glow,  e, 0, y, 0, r * 2.4f, argb((int) (60 * flicker), ELEC));
        }

        // --- Petites orbes en hélice autour du rayon ---
        final int helix = 26;
        for (int i = 0; i < helix; i++) {
            float y = 2.5f + mod(t * 0.7f + i * 2.4f, helix * 2.4f);
            if (y > height) continue;
            float a = y * 0.9f + t * 0.25f;
            float rad = 0.62f * jitter;
            float x = (float) Math.cos(a) * rad;
            float z = (float) Math.sin(a) * rad;
            sphere(solid, e, x, y, z, 0.05f, argb(255, WHITE));
            sphere(glow,  e, x, y, z, 0.12f, argb((int) (120 * flicker), ELEC));
        }

        ms.pop();
    }

    // ------------------------------------------------------------------ géométrie

    private static void cylinder(VertexConsumer vc, MatrixStack.Entry e, float r,
                                 float y0, float y1, float v0, float v1, int color) {
        for (int i = 0; i < CYL_SIDES; i++) {
            double a0 = Math.PI * 2.0 * i / CYL_SIDES;
            double a1 = Math.PI * 2.0 * (i + 1) / CYL_SIDES;
            float x0 = (float) Math.cos(a0) * r, z0 = (float) Math.sin(a0) * r;
            float x1 = (float) Math.cos(a1) * r, z1 = (float) Math.sin(a1) * r;
            float u0 = i / (float) CYL_SIDES, u1 = (i + 1) / (float) CYL_SIDES;
            quad(vc, e, color,
                x0, y0, z0, u0, v0,
                x0, y1, z0, u0, v1,
                x1, y1, z1, u1, v1,
                x1, y0, z1, u1, v0);
        }
    }

    private static void sphere(VertexConsumer vc, MatrixStack.Entry e,
                               float cx, float cy, float cz, float r, int color) {
        for (int i = 0; i < SPH_LAT; i++) {
            double t0 = Math.PI * i / SPH_LAT;
            double t1 = Math.PI * (i + 1) / SPH_LAT;
            float y0 = (float) Math.cos(t0) * r, y1 = (float) Math.cos(t1) * r;
            float s0 = (float) Math.sin(t0) * r, s1 = (float) Math.sin(t1) * r;
            float v0 = i / (float) SPH_LAT, v1 = (i + 1) / (float) SPH_LAT;
            for (int j = 0; j < SPH_LON; j++) {
                double p0 = Math.PI * 2.0 * j / SPH_LON;
                double p1 = Math.PI * 2.0 * (j + 1) / SPH_LON;
                float c0 = (float) Math.cos(p0), n0 = (float) Math.sin(p0);
                float c1 = (float) Math.cos(p1), n1 = (float) Math.sin(p1);
                float u0 = j / (float) SPH_LON, u1 = (j + 1) / (float) SPH_LON;
                quad(vc, e, color,
                    cx + s0 * c0, cy + y0, cz + s0 * n0, u0, v0,
                    cx + s1 * c0, cy + y1, cz + s1 * n0, u0, v1,
                    cx + s1 * c1, cy + y1, cz + s1 * n1, u1, v1,
                    cx + s0 * c1, cy + y0, cz + s0 * n1, u1, v0);
            }
        }
    }

    /** Émet le quad dans les deux sens pour être visible de l'intérieur comme de l'extérieur. */
    private static void quad(VertexConsumer vc, MatrixStack.Entry e, int color,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv) {
        vtx(vc, e, color, ax, ay, az, au, av);
        vtx(vc, e, color, bx, by, bz, bu, bv);
        vtx(vc, e, color, cx, cy, cz, cu, cv);
        vtx(vc, e, color, dx, dy, dz, du, dv);

        vtx(vc, e, color, dx, dy, dz, du, dv);
        vtx(vc, e, color, cx, cy, cz, cu, cv);
        vtx(vc, e, color, bx, by, bz, bu, bv);
        vtx(vc, e, color, ax, ay, az, au, av);
    }

    private static void vtx(VertexConsumer vc, MatrixStack.Entry e, int color,
                            float x, float y, float z, float u, float v) {
        vc.vertex(e, x, y, z)
          .color(color)
          .texture(u, v)
          .overlay(OverlayTexture.DEFAULT_UV)
          .light(LIGHT)
          .normal(e, 0.0f, 1.0f, 0.0f);
    }

    // ------------------------------------------------------------------ utilitaires

    private static int argb(int a, int[] rgb) {
        a = Math.max(0, Math.min(255, a));
        return (a << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
    }

    private static float mod(float v, float m) {
        float r = v % m;
        return r < 0 ? r + m : r;
    }
}
