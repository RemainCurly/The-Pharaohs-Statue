#version 430

in vec2 tc;
in float transp;
in float isLight;
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout (binding=0) uniform sampler2D s;

void main(void)
{
	if(isLight > 0.5)
		color = vec4(0.0, 0.0, 1.0, 1.0);
	else
	{
		vec4 temp = texture(s,tc);
		color = vec4(temp.xyz, transp);
	}
}
