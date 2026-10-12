public class Heatwave extends Weather {

	public Heatwave(int strength, int duration, int[][] location) {
		super(strength, duration, location);
	}

	@Override
	public void affectSimulation(ForestFireSimulation simulation) {
		for (int[] cell : location) {
			simulation.addHeat(cell[0], cell[1], strength);
		}
	}
}