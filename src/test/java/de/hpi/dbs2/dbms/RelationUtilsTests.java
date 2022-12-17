package de.hpi.dbs2.dbms;

import com.google.common.collect.Lists;
import de.hpi.dbs2.dbms.utils.RelationUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RelationUtilsTests {

	DBMS dbms = new DBMS(3, 2);

	@Test
	void testFill() {
		ColumnDefinition columns = new ColumnDefinition(ColumnDefinition.ColumnType.INTEGER);

		List<Tuple> tuples = List.of(
			new Tuple(1, 1),
			new Tuple(1, 2),
			new Tuple(1, 3)
		);

		Relation relation = dbms.createRelation(dbms.getBlockManager(), columns);
		RelationUtils.fill(relation, dbms.getBlockManager(), it -> {
			for(Tuple tuple : tuples) {
				it.add(tuple);
			}
		});
		assertEquals(0, dbms.getBlockManager().getUsedBlocks());

		List<Block> blocks = Lists.newArrayList(relation);
		assertEquals(2, blocks.size());

		Block block1 = blocks.get(0);
		dbms.getBlockManager().load(block1);
		assertEquals(2, block1.getSize());
		assertEquals(tuples.get(0), block1.get(0));
		assertEquals(tuples.get(1), block1.get(1));
		block1.close();

		Block block2 = blocks.get(1);
		dbms.getBlockManager().load(block2);
		assertEquals(1, block2.getSize());
		assertEquals(tuples.get(2), block2.get(0));
		block2.close();
	}

	@Test
	void testTupleIterator() {
		ColumnDefinition columns = new ColumnDefinition(ColumnDefinition.ColumnType.INTEGER);

		List<Tuple> tuples = List.of(
			new Tuple(1, 1),
			new Tuple(1, 2),
			new Tuple(1, 3),
			new Tuple(1, 4),
			new Tuple(1, 5)
		);

		Relation relation = dbms.createRelation(dbms.getBlockManager(), columns);
		RelationUtils.fill(relation, dbms.getBlockManager(), it -> {
			for(Tuple tuple : tuples) {
				it.add(tuple);
			}
		});

		Iterator<Tuple> iterator = RelationUtils.tupleIterator(relation, dbms.getBlockManager());
		List<Tuple> tuplesFromIterator = Lists.newArrayList(iterator);
		assertEquals(0, dbms.getBlockManager().getUsedBlocks());

		Assertions.assertIterableEquals(tuples, tuplesFromIterator);
	}

	@Test
	void testEqualContent() {
		ColumnDefinition columns = new ColumnDefinition(ColumnDefinition.ColumnType.INTEGER);

		List<Tuple> tuples = List.of(
			new Tuple(1, 1),
			new Tuple(1, 2),
			new Tuple(1, 3),
			new Tuple(1, 4),
			new Tuple(1, 5)
		);

		Relation relation1 = dbms.createRelation(dbms.getBlockManager(), columns);
		RelationUtils.fill(relation1, dbms.getBlockManager(), it -> {
			for(Tuple tuple : tuples) {
				it.add(tuple);
			}
		});
		Relation relation2 = dbms.createRelation(dbms.getBlockManager(), columns);
		RelationUtils.fill(relation2, dbms.getBlockManager(), it -> {
			for(Tuple tuple : tuples) {
				it.add(tuple);
			}
		});
		Relation relation3 = dbms.createRelation(dbms.getBlockManager(), columns);
		RelationUtils.fill(relation3, dbms.getBlockManager(), it -> {
			for(Tuple tuple : tuples) {
				it.add(tuple);
			}
			it.add(new Tuple(1, 6));
		});
		Relation relation4 = dbms.createRelation(dbms.getBlockManager(), columns);
		RelationUtils.fill(relation3, dbms.getBlockManager(), it -> {
			it.add(new Tuple(1, 0));
			for(Tuple tuple : tuples) {
				it.add(tuple);
			}
		});

		assertTrue(RelationUtils.equalContent(
			dbms.getBlockManager(), relation1, relation2
		));
		assertFalse(RelationUtils.equalContent(
			dbms.getBlockManager(), relation1, relation3
		));
		assertFalse(RelationUtils.equalContent(
			dbms.getBlockManager(), relation1, relation4
		));
	}
}
