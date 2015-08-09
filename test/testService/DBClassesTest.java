package testService;

import org.junit.*;
import org.junit.runner.RunWith;

import db.DBArtist;
import db.DBNetwork;
import db.DBNode;
import db.DBOutcome;
import db.DBTag;
import db.DBUser;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class DBClassesTest {
	@Test  
	public void testArtist() {
		DBArtist artist = new DBArtist(1,"name",50);
		Assert.assertEquals(artist.getId(), 1);
		Assert.assertEquals(artist.getName(), "name");
		Assert.assertEquals(artist.getListenCount(), 50);
	}
	@Test  
	public void testNetwork() {
		DBNetwork network = new DBNetwork(1,"name");
		Assert.assertEquals(network.getId(), 1);
		Assert.assertEquals(network.getName(), "name");

		
		DBNode node = new DBNode(2, "nodeName1");
		Assert.assertEquals(node.getName(), "nodeName1");
		Assert.assertEquals(node.getId(), 2);

		
		network.addNodes(node);
		network.addNodes(new DBNode(3, "nodeName2"));
		network.addNodes(new DBNode(4, "nodeName2"));

		Assert.assertEquals(network.getNodes().size(), 3);

		Assert.assertEquals(network.getNodes().get(0), node);
		Assert.assertEquals(network.getNodes().get(1).getId(), 3);
		Assert.assertEquals(network.getNodes().get(2).getId(), 4);
	}
	
	@Test  
	public void testNode() {
		DBNode node = new DBNode(2, "nodeName1");
		
		DBOutcome outcome = new DBOutcome(5, "rosso");
		node.addOutcome(outcome);
		node.addOutcome(new DBOutcome(9, "blu"));
		Assert.assertEquals(node.getOutcomes().get(0),outcome);
		Assert.assertEquals(node.getOutcomes().get(1).getId(),9);
		Assert.assertArrayEquals(node.getOutcomesArray(),new String[]{"rosso","blu"});
		
		DBNode parent = new DBNode(6, "pippo");
		node.addParent(parent);
		node.addParent(new DBNode(8, "mario"));
		Assert.assertEquals(node.getParents().get(0),parent);
		Assert.assertEquals(node.getParents().get(1).getId(),8);
		Assert.assertArrayEquals(node.getParentsArray(),new String[]{"pippo","mario"});

		node.addProbability(0.5);
		node.addProbability(0.3);
		node.addProbability(0.2);
		Assert.assertEquals(node.getProbabilities().get(0),0.5, 0.0001);
		Assert.assertEquals(node.getProbabilities().get(1),0.3, 0.0001);
		Assert.assertEquals(node.getProbabilities().get(2),0.2, 0.0001);
		Assert.assertArrayEquals(node.getProbabilitiesArray(),new double[]{0.5,0.3,0.2},0.0001);
	}
	
	@Test  
	public void testUserComplete() {
		DBUser user = new DBUser(1,"pippo",25,"m");
		Assert.assertEquals(user.getId(),1);
		Assert.assertEquals(user.getName(),"pippo");
		Assert.assertEquals(user.getAge().longValue(),25);
		Assert.assertEquals(user.getGender(),"m");
	}
	
	@Test  
	public void testTag() {
		DBTag tag = new DBTag(1);
		tag.setName("pop");
		Assert.assertEquals(tag.getId(),1);
		Assert.assertEquals(tag.getName(),"pop");
	}
}
