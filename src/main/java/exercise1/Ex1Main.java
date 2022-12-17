package exercise1;

import de.hpi.dbs2.dbms.*;
import de.hpi.dbs2.dbms.utils.RelationUtils;
import de.hpi.dbs2.exercise1.SortOperation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Ex1Main {

	public static void main(String[] args) {
		File inputRelationFile = new File(".\\src\\test\\resources\\exercise1\\input.csv");
		System.out.println("Using \"" + inputRelationFile + "\" as input relation.");

		DBMS dbms = new DBMS(3, 2);

		ColumnDefinition columnDefinition = new ColumnDefinition(ColumnDefinition.ColumnType.INTEGER, ColumnDefinition.ColumnType.STRING, ColumnDefinition.ColumnType.DOUBLE);

		Relation inputRelation = dbms.createRelation(dbms.getBlockManager(), columnDefinition);
		try(InputStream relationInputStream = new FileInputStream(inputRelationFile)) {
			RelationUtils.loadCSV(inputRelation, dbms.getBlockManager(), relationInputStream);
		} catch(IOException ex) {
			System.err.println("File could not be read.");
			System.exit(2);
		}

		System.out.println("Input relation:");
		RelationUtils.printlnAllBlocks(inputRelation);

		Relation outputRelation = dbms.createRelation(dbms.getBlockManager(), columnDefinition);

		final int sortColumnIndex = 0;
		SortOperation sortOperation = new TPMMSJava(dbms.getBlockManager(), sortColumnIndex);

		// sortOperation.execute(inputRelation, outputRelation);

		System.out.println("Output relation:");
		RelationUtils.printlnAllBlocks(outputRelation);
	}
}
