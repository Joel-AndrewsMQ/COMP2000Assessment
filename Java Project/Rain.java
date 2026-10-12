public class Rain extends Weather{

	public Rain(int strength, int duration, int[][] location) {
		super(strength, duration, location);
	}

	@Override
	public void affectSimulation(ForestFireSimulation simulation){
		for(int[] cell : location){					
		simulation.removeHeat(cell[0], cell[1], strength);
	}		
	}
}