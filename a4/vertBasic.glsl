#version 430

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 tex_coord;
out vec2 tc;
out float transp;
out float isLight;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform float alpha; //Transparency amount determined from JOGL side
uniform float light;
layout (binding=0) uniform sampler2D s;

void main(void)
{	
	gl_Position = proj_matrix * mv_matrix * vec4(position,1.0);
	tc = tex_coord;
	transp = alpha;
	isLight = light;
}