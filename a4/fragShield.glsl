#version 430
 
out vec4 fragColor;
in float isShield;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;

layout (binding=0) uniform sampler2D s;

void main(void)
{
	if(isShield > 0.5)
		fragColor = vec4(.01, .79, .67, 1.0); //RGB for cyan
	else
		fragColor = vec4(1.0, 1.0, 0.0, 1.0); //RGB for yellow 
}
