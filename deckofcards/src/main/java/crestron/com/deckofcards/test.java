package crestron.com.deckofcards;

public class test {

	public static void main(String[] args) throws CardNotFoundException {
		// TODO Auto-generated method stub
		Deck d = new Deck(true, 1);
		Deck q = new Deck(true, 2);
		
		for(int i=0;i<d.deckCount();i++) {
			System.out.println(d.draw() + "\t" + q.draw());
		}
		
		Deck j = new Deck(true);
		j.shuffle(1L);
		for(int i=0;i<d.deckCount();i++) {
			//System.out.println(j.draw());
		}
		
		Deck t = new Deck(true);
		//t.shuffle(1);
		/*for(int i=0;i<d.deckCount();i++) {
			Card c = t.draw();
			//System.out.println(c + "\t" + c.getImage().toString());
		}
		*/
		
		Hand h = new Hand("Jacob");
		t.dealHand(h, 5);
		
		System.out.println(h);
		h.sortHandByValue();
		System.out.println(h.toString());
		h.sortHandBySuit();
		System.out.println(h.toString());
		h.clearHand();
		System.out.println(h.toString());

	}

}
