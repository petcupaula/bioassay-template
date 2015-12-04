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

import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.geometry.*;
import javafx.util.*;

/*
	Lookup panel: takes a partially specified schema value and opens up the vocabulary list, to make it easy to pick URI/label/description
	combinations.
*/

public class LookupPanel extends Dialog<LookupPanel.Resource[]>
{
	private Vocabulary vocab = null;
	private Set<String> usedURI;
	private boolean multi;

	public static final class Resource
	{
		public String uri, label, descr;
		public boolean beingUsed;
		
		public Resource(String uri, String label, String descr)
		{
			this.uri = uri;
			this.label = label == null ? "" : label;
			this.descr = descr == null ? "" : descr;
		}
		/*String getURI() {return uri;}
		void setURI(String uri) {this.uri = uri;}
		String getLabel() {return label;}
		void setLabel(String label) {this.label = label;}
		String getDescr() {return descr;}
		void setDescr(String descr) {this.descr = descr;}*/
	};
	private List<Resource> resources = new ArrayList<>();

	private TabPane tabber = new TabPane();
	private Tab tabList = new Tab("List"), tabTree = new Tab("Hierarchy");

	private TextField fieldSearch = new TextField();
	private TableView<Resource> tableList = new TableView<>();

    private TreeItem<Vocabulary.Branch> treeRoot = new TreeItem<Vocabulary.Branch>(new Vocabulary.Branch(null));
    private TreeView<Vocabulary.Branch> treeView = new TreeView<Vocabulary.Branch>(treeRoot);

    private final class HierarchyTreeCell extends TreeCell<Vocabulary.Branch>
    {
        public void updateItem(Vocabulary.Branch branch, boolean empty)
        {
            super.updateItem(branch, empty);
            
            if (branch != null)
            {
                String text = "URI <" + branch.uri + ">";
    			String descr = vocab.getDescr(branch.uri);
                if (descr != null && descr.length() > 0) text += "\n\n" + descr;
                Tooltip tip = new Tooltip(text);
                tip.setWrapText(true);
                tip.setMaxWidth(400);
    			Tooltip.install(this, tip);
            }
     
            if (empty)
            {
                setText(null);
                setGraphic(null);
            }
            else 
            {
            	String label = vocab.getLabel(branch.uri);

    			String style = "-fx-text-fill: black; -fx-font-weight: normal;";
    			if (usedURI.contains(branch.uri)) style = "-fx-text-fill: #000080; -fx-font-weight: bold;";
            	
            	setText(label);
   				setStyle(style);
                setGraphic(getTreeItem().getGraphic());
    	    }
    	    
        }
    	/*private String getString() 
        {
            return getItem() == null ? "" : getItem().toString();
        }*/
    }

    private final int PADDING = 2;
       
	// ------------ public methods ------------

	public LookupPanel(String searchText, Set<String> usedURI, boolean multi)
	{
		super();
		
		this.usedURI = usedURI;
		this.multi = multi;
		
		loadResources();
		
		setTitle("Lookup URI");

		setResizable(true);

		for (Tab tab : new Tab[]{tabList, tabTree}) {tab.setClosable(false);}

		setupList(searchText);
		setupTree(searchText);

		tabber.getTabs().addAll(tabList, tabTree);

		getDialogPane().setContent(tabber);

		getDialogPane().getButtonTypes().add(new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE));
		
		// setup the buttons
		
		ButtonType btnTypeUse = new ButtonType("Use", ButtonBar.ButtonData.OK_DONE);
		getDialogPane().getButtonTypes().add(btnTypeUse);
		setResultConverter(buttonType ->
		{
			if (buttonType == btnTypeUse) return composeCurrentValue();
			return null;
		});
		Button btnUse = (Button)getDialogPane().lookupButton(btnTypeUse);
		btnUse.addEventFilter(ActionEvent.ACTION, event ->
		{
			if (tableList.getSelectionModel().getSelectedIndex() < 0 &&
				treeView.getSelectionModel().getSelectedIndex() < 0) event.consume();
		});
		tableList.setOnMousePressed(event ->
		{
			if (event.isPrimaryButtonDown() && event.getClickCount() == 2) btnUse.fire();
		});
		tableList.setOnKeyPressed(event ->
		{
			if (event.getCode() == KeyCode.ENTER) btnUse.fire();
		});
		
        tableList.getSelectionModel().setSelectionMode(multi ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);
        treeView.getSelectionModel().setSelectionMode(multi ? SelectionMode.MULTIPLE : SelectionMode.SINGLE);
		
        Platform.runLater(() -> fieldSearch.requestFocus());
	}
	
	// ------------ private methods ------------

	private void loadResources()
	{
		try {vocab = Vocabulary.globalInstance();}
		catch (IOException ex) {ex.printStackTrace(); return;}
		
		for (String uri : vocab.getAllURIs())
		{
			Resource res = new Resource(uri, vocab.getLabel(uri), vocab.getDescr(uri));
			res.beingUsed = usedURI.contains(uri);
			resources.add(res);
		}
	}

	private void setupList(String searchText)
	{
		Lineup line = new Lineup(PADDING);
		line.add(fieldSearch, "Search:", 1, 0);
 
        tableList.setEditable(false);
 
        TableColumn<Resource, String> colUsed = new TableColumn<>("U");
		colUsed.setMinWidth(20);
		colUsed.setPrefWidth(20);
        colUsed.setCellValueFactory(resource -> {return new SimpleStringProperty(resource.getValue().beingUsed ? "\u2713" : "");});

        TableColumn<Resource, String> colURI = new TableColumn<>("URI");
		colURI.setMinWidth(150);
        colURI.setCellValueFactory(resource -> {return new SimpleStringProperty(substitutePrefix(resource.getValue().uri));});
         
        TableColumn<Resource, String> colLabel = new TableColumn<>("Label");
		colLabel.setMinWidth(200);
        colLabel.setCellValueFactory(resource -> {return new SimpleStringProperty(resource.getValue().label);});
        
        TableColumn<Resource, String> colDescr = new TableColumn<>("Description");
		colDescr.setMinWidth(400);
        colDescr.setCellValueFactory(resource -> {return new SimpleStringProperty(cleanupDescription(resource.getValue().descr));});

		tableList.setMinHeight(450);        
        tableList.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tableList.getColumns().addAll(colUsed, colURI, colLabel, colDescr);
        tableList.setItems(FXCollections.observableArrayList(searchedSubset(searchText)));

        BorderPane pane = new BorderPane();
        pane.setPrefSize(800, 500);
        pane.setMaxHeight(Double.MAX_VALUE);
        pane.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        BorderPane.setMargin(line, new Insets(0, 0, PADDING, 0));
        pane.setTop(line);
        pane.setCenter(tableList);
        
        tabList.setContent(pane);

		fieldSearch.setText(searchText);
		fieldSearch.textProperty().addListener((observable, oldValue, newValue) -> 
		{
			tableList.setItems(FXCollections.observableArrayList(searchedSubset(newValue)));
		});
	}

	private void setupTree(String searchText)
	{
		for (Vocabulary.Branch branch : vocab.getRootBranches()) 
		{
			TreeItem<Vocabulary.Branch> item = populateTreeBranch(treeRoot, branch);
			item.setExpanded(true); // open up just the first level
		}
		
		treeView.setShowRoot(false);
		treeView.setCellFactory((p) -> new HierarchyTreeCell());
	
        BorderPane pane = new BorderPane();
        pane.setPrefSize(800, 500);
        pane.setMaxHeight(Double.MAX_VALUE);
        pane.setPadding(new Insets(PADDING, PADDING, PADDING, PADDING));
        pane.setCenter(treeView);
        
        tabTree.setContent(pane);
	
/*
		treeView.setCellFactory(new Callback<TreeView<Branch>, TreeCell<Branch>>()
		{
            public TreeCell<Branch> call(TreeView<Branch> p) {return new HierarchyTreeCell();}
        });
		treeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Branch>>()
		{
	        public void changed(ObservableValue<? extends TreeItem<Branch>> observable, TreeItem<Branch> oldVal, TreeItem<Branch> newVal) 
	        {
	        	if (oldVal != null) pullDetail(oldVal);
	        	if (newVal != null) pushDetail(newVal);
            }
		});	
		treeView.focusedProperty().addListener((val, oldValue, newValue) -> Platform.runLater(() -> maybeUpdateTree()));*/	
	
	}
	
	// recursively add a new branch into the tree
	private TreeItem<Vocabulary.Branch> populateTreeBranch(TreeItem<Vocabulary.Branch> parent, Vocabulary.Branch branch)
	{
		TreeItem<Vocabulary.Branch> item = new TreeItem<>(branch);
		parent.getChildren().add(item);
		
		if (usedURI.contains(branch.uri))
		{
			TreeItem<Vocabulary.Branch> look = item.getParent();
			while (look != null)
			{
				look.setExpanded(true);
				look = look.getParent();
			}
		}
		
		for (Vocabulary.Branch child : branch.children) populateTreeBranch(item, child);
		return item;
	}
	
	// manufactures a value from the selected items
	private LookupPanel.Resource[] composeCurrentValue()
	{
		if (tabber.getSelectionModel().getSelectedItem() == tabList)
		{
			List<LookupPanel.Resource> list = tableList.getSelectionModel().getSelectedItems();
			return list.toArray(new LookupPanel.Resource[list.size()]);
		}
		else if (tabber.getSelectionModel().getSelectedItem() == tabTree)
		{
			List<TreeItem<Vocabulary.Branch>> list = treeView.getSelectionModel().getSelectedItems();
			
			List<LookupPanel.Resource> ret = new ArrayList<>();
			for (int n = 0; n < list.size(); n++)
			{
				Vocabulary.Branch branch = list.get(n).getValue();
				
				if (multi && usedURI.contains(branch.uri)) continue;
				
				ret.add(new Resource(branch.uri, vocab.getLabel(branch.uri), vocab.getDescr(branch.uri)));
			}
			return ret.toArray(new LookupPanel.Resource[ret.size()]);
		}
		return null;
	}

	// returns a subset of the resources which matches the search text (or all if blank)
	private List<Resource> searchedSubset(String searchText)
	{
		if (searchText.length() == 0) return resources;
		
		String searchLC = searchText.toLowerCase();
		
		List<Resource> subset = new ArrayList<>();
		for (Resource res : resources)
		{
			if (res.label.toLowerCase().indexOf(searchLC) >= 0 || res.uri.toLowerCase().indexOf(searchLC) >= 0 ||
				res.descr.toLowerCase().indexOf(searchLC) >= 0) subset.add(res);
		}
		return subset;
	}
	
	// switches shorter prefixes for display convenience
	private final String[] SUBST = 
    {
    	"obo:", "http://purl.obolibrary.org/obo/",
    	"bao:", "http://www.bioassayontology.org/bao#",
    	"uo:",	"http://purl.org/obo/owl/UO#"
    };
	private String substitutePrefix(String uri)
	{
		for (int n = 0; n < SUBST.length; n += 2)
		{
			if (uri.startsWith(SUBST[n + 1])) return SUBST[n] + uri.substring(SUBST[n + 1].length());
		}
		return uri;
	}
	
	private String cleanupDescription(String descr)
	{
		return descr.replaceAll("\n", " ");
	}
}
