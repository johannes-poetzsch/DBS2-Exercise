package exercise3;

import de.hpi.dbs2.dbms.*;
import de.hpi.dbs2.dbms.utils.RelationUtils;
import de.hpi.dbs2.exercise3.JoinOperation;
import de.hpi.dbs2.exercise3.NestedLoopEquiInnerJoin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Ex3Main {

	public static void main(String[] args) {
		File inputRelation1File = new File("./src/test/resources/exercise3/title.basics.sample.tsv");
		System.out.println("Using \"" + inputRelation1File + "\" as input relation 1.");
		File inputRelation2File = new File("./src/test/resources/exercise3/title.principals.sample.tsv");
		System.out.println("Using \"" + inputRelation2File + "\" as input relation 2.");

		DBMS dbms = new DBMS(5, 10);

		ColumnDefinition leftColumnDefinition = new ColumnDefinition(
			ColumnDefinition.ColumnType.STRING,
			ColumnDefinition.ColumnType.STRING
		);
		ColumnDefinition rightColumnDefinition = new ColumnDefinition(
			ColumnDefinition.ColumnType.STRING,
			ColumnDefinition.ColumnType.STRING,
			ColumnDefinition.ColumnType.STRING,
			ColumnDefinition.ColumnType.STRING
		);

		Relation leftInputRelation = dbms.createRelation(dbms.getBlockManager(), leftColumnDefinition);
		Relation rightInputRelation = dbms.createRelation(dbms.getBlockManager(), rightColumnDefinition);
		try(
			InputStream relation1InputStream = new FileInputStream(inputRelation1File);
			InputStream relation2InputStream = new FileInputStream(inputRelation2File)
		) {
			RelationUtils.loadCSV(
				leftInputRelation,
				dbms.getBlockManager(),
				relation1InputStream,
				List.of(0, 2),
				"\t",
				true
			);
			RelationUtils.loadCSV(
				rightInputRelation,
				dbms.getBlockManager(),
				relation2InputStream,
				List.of(0, 3, 4, 5),
				"\t",
				true
			);
		} catch(IOException ex) {
			System.err.println("Files could not be read.");
			return;
		}

		System.out.println("Input relation 1:");
		RelationUtils.printlnAllBlocks(leftInputRelation);
		System.out.println();
		System.out.println("Input relation 2:");
		RelationUtils.printlnAllBlocks(rightInputRelation);
		System.out.println();

		int leftColumnIndex = 0;
		int rightColumnIndex = 0;
		JoinOperation nleij = new NestedLoopEquiInnerJoin(dbms.getBlockManager(), leftColumnIndex, rightColumnIndex);
		int hashBucketCount = 10;
		JoinOperation heij = new HashEquiInnerJoinJava(dbms.getBlockManager(), leftColumnIndex, rightColumnIndex);

		ColumnDefinition outputColumnDefinition = nleij.buildOutputColumns(leftInputRelation, rightInputRelation);
		// ColumnDefinition outputColumnDefinition = heij.buildOutputColumns(inputRelation1, inputRelation2);
		Relation outputRelation = dbms.createRelation(dbms.getBlockManager(), outputColumnDefinition);

		nleij.join(leftInputRelation, rightInputRelation, outputRelation);
		// heij.join(inputRelation1, inputRelation2, outputRelation);

		System.out.println("Output relation:");
		RelationUtils.printlnAllBlocks(outputRelation);
	}
}
