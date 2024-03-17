// flare

#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;
uniform float u_brightness;

varying vec2 v_texCoord0;



void main()
{
	vec4 color = texture2D(u_texture, v_texCoord0);
    color.a  *= u_brightness;
    gl_FragColor = color;
}
