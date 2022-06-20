#version 430

layout (location=0) in vec4 vertPos;
layout (location=1) in vec4 vertNormal;

out vec3 varyingNormal;
out float passedShield;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform float shield;

void main(void)
{	
	varyingNormal = (norm_matrix * vertNormal).xyz;
	passedShield = shield;		//Using float as a boolean to determine if the object is the Scarab shield or not 
	
	gl_Position = mv_matrix * vertPos;
}
