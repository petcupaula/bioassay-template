/*
 * BioAssay Ontology Annotator Tools
 * 
 * (c) 2014-2015 Collaborative Drug Discovery Inc.
 */

package com.cdd.bao.editor;

import com.cdd.bao.*;
import com.cdd.bao.template.*;

import java.io.*;
import java.util.*;

import javafx.event.*;
import javafx.geometry.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.application.*;
import javafx.beans.value.*;
import javafx.util.*;

/*
	Edit Schema: the primary window for BAO schema editing, which is responsible for taking care of a data instance.
*/

public class EditSchema
{
	// ------------ private data ------------	

	private File schemaFile = null;
	private StackSchema stack = new StackSchema();

    private Stage stage;
    private BorderPane root;
    private SplitPane splitter;
    private TreeView<String> treeview;
    private SchemaBranch treeroot;
    private DetailPane detail;
    
    private MenuBar menuBar;
    private Menu menuFile, menuEdit, menuValue;
    private MenuItem miFileNew;
    
    private final class SchemaBranch extends TreeItem<String>
    {
    	Schema.Group group = null;
    	Schema.Assignment assignment = null;
    	String locatorID = null;

		SchemaBranch(String label)
		{
			super(label);
		}    	
    	SchemaBranch(Schema.Group category, String locatorID)
    	{
    		super(category.groupName);
    		this.group = category;
    		this.locatorID = locatorID;
    	}
    	SchemaBranch(Schema.Assignment assignment, String locatorID)
    	{
    		super(assignment.assnName);
    		this.assignment = assignment;
    		this.locatorID = locatorID;
    	}
    }

	// ------------ public methods ------------	

	public EditSchema(Stage stage)
	{
		this.stage = stage;

        stage.setTitle("BioAssay Schema Editor");        

		menuBar = new MenuBar();
		menuBar.setUseSystemMenuBar(true);
		menuBar.getMenus().add(menuFile = new Menu("_File"));
		menuBar.getMenus().add(menuEdit = new Menu("_Edit"));
		menuBar.getMenus().add(menuValue = new Menu("_Value"));
		//menuBar.getMenus().add(menuWindow = new Menu("_Window"));
		createMenuItems();

		treeroot = new SchemaBranch("Assay"); // !! THIS SHOULD show the root...
		treeview = new TreeView<String>(treeroot);
		treeview.setEditable(true);
		treeview.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>()
		{
            public TreeCell<String> call(TreeView<String> p) {return new AssayHierarchyTreeCell();}
        });
        /*treeview.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            public void handle(MouseEvent event)
            {            
				if (event.getClickCount() == 2) treeDoubleClick(event);
            }
        });
        treeview.setOnMousePressed(new EventHandler<MouseEvent>()
		{
			public void handle(MouseEvent event)
			{
				if (event.getButton().equals(MouseButton.SECONDARY) || event.isControlDown()) treeRightClick(event);
			}
		});*/
		treeview.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<String>>()
		{
	        public void changed(ObservableValue<? extends TreeItem<String>> observable, TreeItem<String> oldVal, TreeItem<String> newVal) 
	        {
	        	pullDetail((SchemaBranch)oldVal);
	        	pushDetail((SchemaBranch)newVal);
            }
		});		

		detail = new DetailPane();

		StackPane sp1 = new StackPane(), sp2 = new StackPane();
		sp1.getChildren().add(treeview);
		sp2.getChildren().add(detail);
		
		splitter = new SplitPane();
		splitter.setOrientation(Orientation.HORIZONTAL);
		splitter.getItems().addAll(sp1, sp2);
		splitter.setDividerPositions(0.4, 1.0);

		root = new BorderPane();
		root.setTop(menuBar);
		root.setCenter(splitter);

		Scene scene = new Scene(root, 800, 600, Color.WHITE);

		stage.setScene(scene);
		
		rebuildTree();
		
		stage.setOnCloseRequest(event -> actionFileClose());
		
		// instantiate vocabulary in a background thread: we don't need it immediately, but prevent blocking later
		new Thread(new Runnable()
		{
			public void run() {try {Vocabulary.globalInstance();} catch (IOException ex) {}}
		}).start();
 	}

	public void loadFile(File f)
	{
		try
		{
			schemaFile = f;
			Schema schema = Schema.deserialise(f);
			stack.setSchema(schema);
			
			//Util.writeln("SCHEMA:\n" + schema.toString());
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
			return;
		}
		
		rebuildTree();
	}

	// ------------ private methods ------------	

	private void createMenuItems()
    {
    	final KeyCombination.Modifier cmd = KeyCombination.SHORTCUT_DOWN, shift = KeyCombination.SHIFT_DOWN;
    
    	addMenu(menuFile, "_New", new KeyCharacterCombination("N", cmd)).setOnAction(event -> actionFileNew());
    	addMenu(menuFile, "_Open", new KeyCharacterCombination("O", cmd)).setOnAction(event -> actionFileOpen());
    	addMenu(menuFile, "_Save", new KeyCharacterCombination("S", cmd)).setOnAction(event -> actionFileSave(false));
    	addMenu(menuFile, "Save _As", new KeyCharacterCombination("S", cmd, shift)).setOnAction(event -> actionFileSave(true));
		menuFile.getItems().add(new SeparatorMenuItem());
    	addMenu(menuFile, "_Close", new KeyCharacterCombination("W", cmd)).setOnAction(event -> actionFileClose());
    	addMenu(menuFile, "_Quit", new KeyCharacterCombination("Q", cmd)).setOnAction(event -> actionFileQuit());
    	
		addMenu(menuEdit, "Add _Group", null).setOnAction(event -> actionGroupAdd());
		addMenu(menuEdit, "Add _Assignment", null).setOnAction(event -> actionAssignmentEdit());
		menuEdit.getItems().add(new SeparatorMenuItem());
    	addMenu(menuEdit, "_Delete", null).setOnAction(event -> actionEditDelete());
    	addMenu(menuEdit, "_Undo", new KeyCharacterCombination("Z", cmd)).setOnAction(event -> actionEditUndo());
    	addMenu(menuEdit, "_Redo", new KeyCharacterCombination("Z", cmd, shift)).setOnAction(event -> actionEditRedo());
		menuEdit.getItems().add(new SeparatorMenuItem());
		addMenu(menuEdit, "Move _Up", null).setOnAction(event -> actionEditMove(-1));
		addMenu(menuEdit, "Move _Down", null).setOnAction(event -> actionEditMove(1));

		addMenu(menuValue, "_Add Value", null).setOnAction(event -> actionValueAdd());
		addMenu(menuValue, "_Delete Value", null).setOnAction(event -> actionValueDelete());
		addMenu(menuValue, "Move _Up", null).setOnAction(event -> actionValueMove(-1));
		addMenu(menuValue, "Move _Down", null).setOnAction(event -> actionValueMove(1));
		menuValue.getItems().add(new SeparatorMenuItem());
		addMenu(menuValue, "_Lookup URI", new KeyCharacterCombination("U", cmd)).setOnAction(event -> actionValueLookupURI());
		addMenu(menuValue, "Lookup _Name", new KeyCharacterCombination("L", cmd)).setOnAction(event -> actionValueLookupName());
    }
    
    private MenuItem addMenu(Menu parent, String title, KeyCharacterCombination accel)
    {
    	MenuItem item = new MenuItem(title);
    	parent.getItems().add(item);
    	if (accel != null) item.setAccelerator(accel);
    	return item;
    }

	private void rebuildTree()
	{
		treeroot.getChildren().clear();
		treeroot.setExpanded(true);
		
		Schema schema = stack.getSchema();
		Schema.Group root = schema.getRoot();
		
		treeroot.setValue(root.groupName);
		treeroot.group = root;
		treeroot.locatorID = schema.locatorID(root);
		fillTreeGroup(schema, root, treeroot);
	}
	
	private void fillTreeGroup(Schema schema, Schema.Group category, TreeItem<String> parent)
	{
		for (Schema.Assignment assn : category.assignments)
		{
			SchemaBranch item = new SchemaBranch(assn, schema.locatorID(assn));
			parent.getChildren().add(item);
		}
		for (Schema.Group subgrp : category.subGroups)
		{		
			SchemaBranch item = new SchemaBranch(subgrp, schema.locatorID(subgrp));
			parent.getChildren().add(item);
		}
	}

	// for the given branch, pulls out the content: if any changes have been made, pushes the modified schema onto the stack
	private void pullDetail() {pullDetail((SchemaBranch)treeview.getSelectionModel().getSelectedItem());}
	private void pullDetail(SchemaBranch branch)
	{
		if (branch == null) return;
		if (branch.group != null)
		{
			Schema.Group modGroup = detail.extractGroup();
			if (modGroup == null) return;

			Schema schema = stack.getSchema();
			if (branch.group.parent != null)
			{
				// reparent the modified group, then swap it out in the parent's child list
				Schema.Group replGroup = schema.obtainGroup(branch.locatorID);
				modGroup.parent = replGroup.parent;
				int idx = replGroup.parent.subGroups.indexOf(replGroup);
				replGroup.parent.subGroups.set(idx, modGroup);
			}
			else schema.setRoot(modGroup);
			
			branch.group = modGroup;
			stack.changeSchema(schema, true);
		}
		if (branch.assignment != null)
		{
			Schema.Assignment modAssn = detail.extractAssignment();
			if (modAssn == null) return;
			
			Schema schema = stack.getSchema();
			
			// reparent the modified assignment, then swap it out in the parent's child list
			Schema.Assignment replAssn = schema.obtainAssignment(branch.locatorID);
			modAssn.parent = replAssn.parent;
			int idx = replAssn.parent.assignments.indexOf(replAssn);
			replAssn.parent.assignments.set(idx, modAssn);
			
			branch.assignment = modAssn;
			stack.changeSchema(schema, true);
		}
	}

	// recreates all the widgets in the detail view, given that the indicated branch has been selected
	private void pushDetail(SchemaBranch branch)
	{
		if (branch.group != null) detail.setGroup(branch.group);
		else if (branch.assignment != null) detail.setAssignment(branch.assignment);
		else detail.clearContent();
	}

	/*private void treeDoubleClick(MouseEvent event)
	{
        TreeItem<String> item = treeview.getSelectionModel().getSelectedItem();
        System.out.println("DOUBLE CLICK : " + item.getValue());
	}        
	private void treeRightClick(MouseEvent event)
	{
        MenuItem addMenuItem = new MenuItem("Fnord!");
        ContextMenu ctx = new ContextMenu();
        ctx.getItems().add(addMenuItem);
        addMenuItem.setOnAction(new EventHandler<ActionEvent>()
        {
            public void handle(ActionEvent t)
            {
            	Util.writeln("--> FNORD!");
            }
        });
        
        TreeItem<String> item = treeview.getSelectionModel().getSelectedItem();
		Util.writeln("RIGHT CLICK");
	}*/
	
	private void actionFileNew()
	{
		Util.writeln("new!");
	}
	private void actionFileSave(boolean promptNew)
	{
		pullDetail();
	
		// dialog in case filename is missing or requested as save-to-other
		if (promptNew || schemaFile == null)
		{
			// do stuff...
			Util.writeln("save:prompt...");
		}
		
		// validity checking
		if (schemaFile == null) return;
		if (!schemaFile.canWrite())
		{
			Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Cannot Save");
            alert.setHeaderText(null);
            alert.setContentText("Not able to write to file: " + schemaFile.getAbsolutePath());
            alert.showAndWait();
            return;
		}
	
		// serialise-to-file
		Schema schema = stack.peekSchema();
		try 
		{
			//schema.serialise(System.out);
			
			OutputStream ostr = new FileOutputStream(schemaFile);
			schema.serialise(ostr);
			ostr.close();
		}
		catch (Exception ex) {ex.printStackTrace();}
	}
	private void actionFileOpen()
	{
		Util.writeln("open!");
	}
	private void actionFileClose()
	{
		// !! prompt if dirty...
		Util.writeln("CLOSE!");
		stage.close();
	}
	private void actionFileQuit()
	{
		// !! prompt if any windows are dirty...
		Platform.exit();
	}
    private void actionGroupAdd()
    {
    	Util.writeln("addgroup!");
    }
    private void actionAssignmentEdit()
    {
    	Util.writeln("addassignment!");
    }
    private void actionEditDelete()
    {
    	Util.writeln("delete!");
    }
    private void actionEditUndo()
    {
    	Util.writeln("undo!");
    }
    private void actionEditRedo()
    {
    	Util.writeln("redo!");
    }
    private void actionEditMove(int dir)
    {
    	Util.writeln("move:"+dir);
    }
    private void actionValueAdd()
    {
    	Util.writeln("addvalue!");
    }
    private void actionValueDelete()
    {
    	Util.writeln("deletevalue!");
    }
    private void actionValueMove(int dir)
    {
    	Util.writeln("movevalue:"+dir);
    }
    private void actionValueLookupURI()
    {
    	Util.writeln("lookupURI!");
    }
    private void actionValueLookupName()
    {
    	Util.writeln("lookupname!");
    }
}
