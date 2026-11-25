package il.ac.tau.cs.software1.inventory;

/*
 * A simple array-based Inventory (Question 6).
 */
public class NaiveInventory implements Inventory {

	private static final int INITIAL_CAPACITY = 100;

	private Collectible[] collectarr;
	private int counter;

	public NaiveInventory() {
		this.collectarr = new Collectible[INITIAL_CAPACITY];
		this.counter = 0;
	}

	@Override
	public void collect(Collectible collectible) {
		if (counter > 0 && counter==collectarr.length){
			Collectible[] newarr = new Collectible[2*counter];
			for (int i=0; i<counter; i++ ){
				newarr[i]=collectarr[i];
			}
			newarr[counter]= collectible;
			collectarr = newarr;
		}
		else{
			collectarr[counter]= collectible;
		}
		counter++;
	}

	@Override
	public float getTotalWeight() {
		float total=0.0f;
		for (int i=0; i<counter; i++){
			float w=collectarr[i].getWeight();
			total+=w;
		}
		return total;
	}

	@Override
	public int getCurrentCount() {
		
		return counter;
	}

	// Returns the array of Collectibles that is stored in the field of this class
	public Collectible[] getCollArr() {
		
		return collectarr;
	}
}
