#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// Vanilla position_tex_color discards fragments below alpha 0.1, which cuts the smooth
// falloff off Astral Sorcery's soft particle textures. Same shader, no alpha test.
void main() {
    fragColor = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
}
