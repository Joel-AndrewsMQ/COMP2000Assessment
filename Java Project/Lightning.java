public class Lightning extends Weather{

	private int strikesPerUpdate;

	public Lightning(int strength, int duration, int[][] location, int strikesPerUpdate){
		super(strength, duration, location);
		this.strikesPerUpdate = strikesPerUpdate;
	}

	public int getStrikesPerUpdate(){
		return strikesPerUpdate;
	}

	@Override
	public void affectSimulation(ForestFireSimulation simulation){		
		for(int i = 0; i < strikesPerUpdate; i++){
			int randomIndex = (int)(Math.random() * location.length);
			int[] coordinate = location[randomIndex];
			simulation.igniteCell(coordinate[0], coordinate[1], strength);
		}		
	}
}
