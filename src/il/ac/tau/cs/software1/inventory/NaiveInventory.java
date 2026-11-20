package il.ac.tau.cs.software1.inventory;

/*
 * A simple array-based Inventory (Question 6).
 */
public class NaiveInventory implements Inventory {

	private static final int INITIAL_CAPACITY = 100;

	private Collectible[] collected_items;
	private int stored_count;

	public NaiveInventory() {
		this.stored_count = 0;
		this.collected_items = new Collectible[INITIAL_CAPACITY];
	}

	@Override
	public void collect(Collectible collectible) {
		int n = this.collected_items.length;
		if(this.stored_count == n) { // the array is full
			Collectible[] tmp = new Collectible[n*2];
			for(int i=0; i<n; i++) {
				tmp[i] = this.collected_items[i];
			}
			this.collected_items = tmp;
		}
		this.collected_items[this.stored_count] = collectible;
		this.stored_count++;
	}

	@Override
	public float getTotalWeight() {
		float count_weight = 0;
		for(int i = 0; i< this.stored_count; i++) {
			count_weight += this.collected_items[i].getWeight();
		}
		return count_weight;
	}

	@Override
	public int getCurrentCount() {
		return this.stored_count;
	}

	// Returns the array of Collectibles that is stored in the field of this class
	public Collectible[] getCollArr() {
		return this.collected_items;
	}
}
