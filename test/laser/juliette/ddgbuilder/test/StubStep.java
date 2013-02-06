package laser.juliette.ddgbuilder.test;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import laser.lj.Binding;
import laser.lj.ContinuationAction;
import laser.lj.InterfaceDeclaration;
import laser.lj.InterfaceDeclarationSet;
import laser.lj.Matcher;
import laser.lj.ResolutionException;
import laser.lj.ScopeBinding;
import laser.lj.Step;
import laser.lj.InterfaceDeclaration.DeclarationKind;
import laser.lj.ast.resolved.StepDeclaration;
import laser.lj.template.AggregateInstanceDescriptor;

public class StubStep implements Step {
	private boolean leaf = false;
	private String name;
	private boolean parallel = false;
	private Step prereq;
	private Step postreq;
	private boolean isRequisite = false;
	private InterfaceDeclarationSet params = new InterfaceDeclarationSet();
	private InterfaceDeclarationSet outParams = new InterfaceDeclarationSet();
	private InterfaceDeclarationSet inParams = new InterfaceDeclarationSet();
	
	public StubStep (String name, boolean leaf) {
		this.name = name;
		this.leaf = leaf;
	}

	public StubStep (boolean parallel, String name) {
		this.name = name;
		this.parallel = parallel;
	}

	public Set<AggregateInstanceDescriptor> getAborters() {
		// TODO Auto-generated method stub
		return null;
	}

	public InterfaceDeclaration getAgentDeclaration() {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<Binding> getBindingsFor(String arg0) throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public InterfaceDeclaration getCardinalityControllingParameter()
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCardinalityPredicate() {
		// TODO Auto-generated method stub
		return null;
	}

	public ScopeBinding getConstraintFor(String arg0)
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public ContinuationAction getContinuationAction() {
		// TODO Auto-generated method stub
		return null;
	}

	public InterfaceDeclaration getDeadlineDeclaration() {
		// TODO Auto-generated method stub
		return null;
	}

	public InterfaceDeclarationSet getDeclarations(DeclarationKind arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public InterfaceDeclaration getExceptionParameter()
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public Step getHandlerFor(AggregateInstanceDescriptor arg0)
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public Step getHandlerFor(Serializable arg0, Matcher arg1)
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Step> getHandlers() throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public InterfaceDeclarationSet getInputParameters() {
		return inParams;
	}

	public InterfaceDeclaration getInterfaceDeclaration(String name) {
		for (InterfaceDeclaration decl : params) {
			if (decl.getName().equals(name)) {
				return decl;
			}
		}
		return null;
	}

	public int getMaximumCardinality() {
		// TODO Auto-generated method stub
		return 0;
	}

	public InterfaceDeclaration getMessageParameter()
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getMinimumCardinality() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getName() {
		return name;
	}

	public InterfaceDeclarationSet getOutputParameters() {
		return outParams;
	}

	public InterfaceDeclarationSet getParameters() {
		return params;
	}

	public Step getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	public Step getPostrequisite() throws ResolutionException {
		// TODO Auto-generated method stub
		return postreq;
	}

	public String getPostrequisiteExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public AggregateInstanceDescriptor getPostrequisiteExpressionException() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Step getPrerequisite() throws ResolutionException {
		// TODO Auto-generated method stub
		return prereq;
	}

	
	public String getPrerequisiteExpression() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public AggregateInstanceDescriptor getPrerequisiteExpressionException() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Step getReactionFor(AggregateInstanceDescriptor arg0)
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Step getReactionFor(Serializable arg0, Matcher arg1)
			throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<Step> getReactions() throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Step getSubstepAt(int arg0) throws ResolutionException,
			IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<Step> getSubsteps() throws ResolutionException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public AggregateInstanceDescriptor getTrigger() {
		// TODO Auto-generated method stub
		return null;
	}

	
	public boolean hasAgentDeclaration() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasBinding(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasCardinalityControllingParameter() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasCardinalityPredicate() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasDeadlineDeclaration() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasExceptionParameter() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasMessageParameter() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasParent() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasPostrequisite() {
		return postreq != null;
	}

	
	public boolean hasPostrequisiteExpression() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean hasPrerequisite() {
		return prereq != null;
	}

	
	public boolean hasPrerequisiteExpression() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public int indexOf(Step arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public boolean isChoice() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isConstrainedParameter(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isHandler() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isLeaf() {
		return leaf;
	}

	
	public boolean isNull() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isOptional() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isParallel() {
		return parallel;
	}

	
	public boolean isReaction() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isReference() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isRepeatable() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isRequisite() {
		return isRequisite;
	}

	
	public boolean isSequential() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isTry() {
		// TODO Auto-generated method stub
		return false;
	}

	
	public boolean isUnbounded() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPrereq(Step prereqStep) {
		prereq = prereqStep;
	}

	public void setIsRequisite(boolean isRequisite) {
		this.isRequisite = isRequisite;
	}

	public void setPostreq(Step postreqStep) {
		postreq = postreqStep;	
	}

	public void addParameter(String name) {
		params.add(new InterfaceDeclaration(DeclarationKind.LOCAL_PARAMETER, name, null));
	}

	public void addOutputParameter(String name) {
		outParams.add(new InterfaceDeclaration(DeclarationKind.OUT_PARAMETER, name, null));
		
	}

	public void addInputParameter(String name) {
		inParams.add(new InterfaceDeclaration(DeclarationKind.IN_PARAMETER, name, null));
	}

	public void addInOutParameter(String name) {
		inParams.add(new InterfaceDeclaration(DeclarationKind.IN_OUT_PARAMETER, name, null));
		outParams.add(new InterfaceDeclaration(DeclarationKind.IN_OUT_PARAMETER, name, null));
	}

	public StepDeclaration getStepDeclaration()
			throws UnsupportedOperationException {
		// TODO Auto-generated method stub
		return null;
	}

}
