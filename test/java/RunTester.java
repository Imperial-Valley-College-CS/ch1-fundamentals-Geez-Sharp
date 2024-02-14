package test.java;
import test.java.TestCase;
import test.java.Tester;
import test.java.Constants;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.event.*;

import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.scene.input.*;

import javafx.collections.FXCollections;

import java.util.*;
import java.io.*;

public class RunTester extends Application
{      
   //Root Parent, Scene and stage
   private Stage appStage = new Stage();
   private BorderPane root = new BorderPane();
   private Scene scene = new Scene( root, Constants.sceneWidth, Constants.sceneHeight);

   //Declare Tester for test class
   private Tester tester;     
   
   //Initialized to selected assignment
   private String selectedAssignment = null;
   
   //Title
   private Label titleLabel = new Label("Test Results");   
      
   //Map that maps every Button(key) to a TestCase(value)
   private HashMap<Button, TestCase> bttnToTestCaseMap = new HashMap<>();
   
   //ArrayList of TestCase objects
   private ArrayList<TestCase> testCases = new ArrayList<TestCase>();
   
   //Test Case Buttons Styling/Formatting/Handler
   private Button[] testCaseButtons;
   private VBox buttonsBox = new VBox();
   private ScrollPane buttonsScroll = new ScrollPane( buttonsBox );
   
   private String prevBttnStyle = "";
   
   //Button Handler
   private TestCaseButtonHandler bttnHandler = new TestCaseButtonHandler();
   private ButtonHoverEntered bttnHoverHandler = new ButtonHoverEntered();
   private ButtonHoverExited bttnNoHoverHandler = new ButtonHoverExited();
   
   //Test class information
   private Label classTitleLabel1 = new Label("Tested Class:");   
   private Label classTitleLabel2 = new Label();
   private HBox classTitleHBox = new HBox(5, classTitleLabel1, classTitleLabel2);
   
   private Label methodTitleLabel1 = new Label("Tested Method:");   
   private Label methodTitleLabel2 = new Label();
   private HBox methodTitleHBox = new HBox(5, methodTitleLabel1, methodTitleLabel2);
   
   private Label paramTitleLabel1 = new Label("Test Inputs:");   
   private Label paramTitleLabel2 = new Label();
   private HBox paramTitleHBox = new HBox(5, paramTitleLabel1, paramTitleLabel2);
   
   private VBox classInfoVBox = new VBox(5, classTitleHBox, methodTitleHBox, paramTitleHBox);
   
   //TextFields for Expected/Output
   private Label expectedLabel = new Label(" Expected Output");
   private TextArea expectedText = new TextArea();
   private VBox expectedVBox = new VBox( 3, expectedLabel, expectedText );
   
   private Label outputLabel = new Label(" Output");
   private TextArea outputText = new TextArea();
   private VBox outputVBox = new VBox(5, outputLabel, outputText);
   private HBox textResultsHBox = new HBox(5, expectedVBox, outputVBox);
   
   private VBox infoVBox = new VBox(5, classInfoVBox, textResultsHBox);
   
   //Dropdown comboBox for list of assignments
   private ComboBox<String> assignmentsComboBox = 
         new ComboBox<String>( FXCollections.observableArrayList( Constants.assignmentsList) );
   private Button introButton = new Button("Ok");
   private Label introLabel = new Label("Select PA");
   private VBox introVBox = new VBox(5, introLabel, assignmentsComboBox, introButton);
   private Scene introScene = new Scene(introVBox);
   private AssignmentBttnHandler bttnHandlerAssignment = new AssignmentBttnHandler();
   
   public static void main(String[] args) {
      Constants.setTesterMap();     //initialize the assignments/tester map
      launch(args);
   }
   
   @Override
   public void start(Stage stage)
   {      
      //Show assignment selection window      
      setIntroStyle();
      introButton.setOnAction( bttnHandlerAssignment );
      root.setStyle( Constants.backgroundColor );
      stage.setScene( introScene );
      stage.show();
   }
   
   public void setIntroStyle()
   {  
      introVBox.setStyle( Constants.backgroundColor );
      introVBox.setPadding( Constants.fiveAround );
      introVBox.setAlignment( Pos.CENTER );
      introLabel.setFont( Constants.titleFont );
   }
      
   public void initialize()
   {
      if( buttonsBox.getChildren() != null )
         buttonsBox.getChildren().clear();
         
      //get TestCases
      testCases = tester.runTestCases();                 
      
      //create text file of test results
      renderTestResultsFile();
      
      //Create and render buttons
      createTestCaseButtons();
      renderButtons();
      
      //render textfields
      renderTextFields( testCases.get(0) );
      
      //add title to application
      titleLabel.setStyle( Constants.backgroundColor );
      titleLabel.setFont( Constants.titleFont );
      
      //set fonts for textAreas and labels for output text
      outputLabel.setFont( Constants.outputLabelFont );
      expectedLabel.setFont( Constants.outputLabelFont );  
      
      classTitleLabel1.setFont( Constants.outputLabelFont );
      methodTitleLabel1.setFont( Constants.outputLabelFont );      
      paramTitleLabel1.setFont( Constants.outputLabelFont );
      
      expectedText.setFont( Constants.textFont );
      outputText.setFont( Constants.textFont );
      
      classTitleLabel2.setFont( Constants.textFont );
      classTitleLabel2.setStyle(Constants.classInfoTextFill);
      methodTitleLabel2.setFont( Constants.textFont );
      methodTitleLabel2.setStyle(Constants.classInfoTextFill);      
      paramTitleLabel2.setFont( Constants.textFont );
      paramTitleLabel2.setStyle(Constants.classInfoTextFill); 
      
      classTitleHBox.setAlignment( Pos.BOTTOM_LEFT );
      methodTitleHBox.setAlignment( Pos.BOTTOM_LEFT );
      paramTitleHBox.setAlignment( Pos.BOTTOM_LEFT );
      
      classInfoVBox.setStyle(Constants.whiteBackground + Constants.borderStyle);
      expectedText.setStyle( Constants.borderStyle );
      outputText.setStyle( Constants.borderStyle );
      infoVBox.setStyle(Constants.backgroundColor);
      buttonsBox.setStyle( Constants.backgroundColor );
      buttonsScroll.setStyle( Constants.backgroundColor );
      
      expectedText.setWrapText( true );
      outputText.setWrapText( true );
      
      root.setTop( titleLabel );        
      root.setLeft( buttonsScroll );
      root.setCenter( infoVBox ); 
      
      BorderPane.setAlignment( titleLabel, Pos.BOTTOM_CENTER );
      root.setStyle(Constants.backgroundColor);
      root.setPadding( Constants.buttnBoxInsets );
      
      appStage.setScene( scene );
      appStage.show();
   }
   
   //render test results file
   public void renderTestResultsFile()
   {
      String path = TestCase.testDirectory;
      String filename = path + "/TestResults.txt";
      
      try
      {
         BufferedWriter outputWriter =
            new BufferedWriter( new FileWriter(filename) );
         
         String prevTestClass = testCases.get(0).getTestedClass();
         int j = 1;
         for( TestCase testCase : testCases )
         {
            if( !testCase.getTestedClass().equals( prevTestClass ) )
               j = 1;
            
            String testClassLabel = testCase.getTestedClass().replace(".java", "");
         
            outputWriter.write(String.format( "****%s - %d****%n", testClassLabel, j));
            outputWriter.write("\tClass: " + testCase.getTestedClass() + "\n");
            outputWriter.write("\tMethod: " + testCase.getMethod() + "\n");
            outputWriter.write("\tInput(s): " + testCase.getInput() + "\n");
            outputWriter.write("\tPassed: " + testCase.getResult());
            outputWriter.newLine();
            outputWriter.newLine();
            
            prevTestClass = testCase.getTestedClass();
            j++;
         }
         outputWriter.flush();
         outputWriter.close();
      }catch( IOException e )
      {
         System.out.println( e.toString() );
      }
   }
   
   //Create Button for each TestCase
   public void createTestCaseButtons()
   {
      testCaseButtons = new Button[ testCases.size() ];  //create array of Buttons for each TestCase
      String prevTestClass = testCases.get(0).getTestedClass();
      int j = 1;
      
      for( int i = 0; i < testCaseButtons.length; i++ )
      {
         if( !testCases.get(i).getTestedClass().equals( prevTestClass ) )
            j = 1;
            
         String testClassLabel = testCases.get(i).getTestedClass().replace(".java", "");
         String buttonLabel = String.format( "%s - %d", testClassLabel, j);
         testCaseButtons[i] = new Button(buttonLabel);
         testCaseButtons[i].setOnAction( bttnHandler );
         testCaseButtons[i].setOnMouseEntered( bttnHoverHandler );
         testCaseButtons[i].setOnMouseExited( bttnNoHoverHandler );
         testCaseButtons[i].setFont( Constants.buttonFont );
         testCaseButtons[i].setMaxWidth( 250 );       //ensures all buttons are same width for given font
         
         bttnToTestCaseMap.put( testCaseButtons[i], testCases.get(i) );
         
         if( testCases.get(i).getResult() )
         {
            testCaseButtons[i].setStyle( Constants.passButtonColor );
         }
         else
         {
            testCaseButtons[i].setStyle(Constants.failButtonColor);
         }
         
         prevTestClass = testCases.get(i).getTestedClass();
         j++;
      }
   }
   
   //Render Buttons for each TestCase
   public void renderButtons()
   {
      buttonsBox.setPadding( Constants.buttnBoxInsets );
      buttonsBox.setSpacing( 3 );
      for( Button b : testCaseButtons )
         buttonsBox.getChildren().add( b );
   }
   
   //Render text in textAreas based on TestCase
   public void renderTextFields( TestCase testCase )
   {
      classTitleLabel2.setText(""+testCase.getTestedClass());
      methodTitleLabel2.setText(""+testCase.getMethod());
      paramTitleLabel2.setText(""+testCase.getInput());
      expectedText.setText( ""+testCase.getExpected() );
      outputText.setText( ""+testCase.getOutput() );
   }
   
   //button handler for various test cases
   class TestCaseButtonHandler implements EventHandler<ActionEvent>
   {
      @Override
      public void handle( ActionEvent e )
      {
         Button pressedBttn = (Button)e.getSource();
         //retrieve TestCase from Button/TestCase map
         TestCase testCase = bttnToTestCaseMap.get( pressedBttn );
         //render the expected output for each test case
         renderTextFields( testCase );
      }
   }
   
   //Button handler to change style of button on hover
   class ButtonHoverEntered implements EventHandler<MouseEvent>
   {
      @Override
      public void handle( MouseEvent e )
      {
         Button bttnHovered = (Button)e.getSource();
         prevBttnStyle = bttnHovered.getStyle();
         bttnHovered.setStyle(Constants.HOVERED_BUTTON_STYLE);
      }
   }
   
   //Button handler to restore previous style of button on hover exit
   class ButtonHoverExited implements EventHandler<MouseEvent>
   {
      @Override
      public void handle( MouseEvent e )
      {
         Button bttnHovered = (Button)e.getSource();
         bttnHovered.setStyle(prevBttnStyle);
      }
   }
   
   //Button handler to select assignment to test
   class AssignmentBttnHandler implements EventHandler<ActionEvent>
   {
      @Override
      public void handle( ActionEvent e )
      {
         //retreive current stage
         Stage stage = (Stage)((Button)e.getSource()).getScene().getWindow();
         //Get assignment selected on combobox
         String assignment = assignmentsComboBox.getValue();           
         if( assignment != null )
         {
            //assign the appropriate tester based on assignment selected
            tester = Constants.testerMap.get(assignment);
            //close current stage
            stage.close();
            //initialize and show appStage
            initialize();
         }
      }
   }
}