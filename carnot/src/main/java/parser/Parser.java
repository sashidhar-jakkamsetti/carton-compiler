package parser;

import java.util.ArrayList;

import javax.lang.model.util.ElementScanner6;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.*;
import dataStructures.Operator.OperatorCode;
import dataStructures.Results.*;
import dataStructures.Token.TokenType;
import exceptions.*;
import intermediateCodeRepresentation.*;

public class Parser 
{
    private static Parser parser;
    private Scanner scanner;
    private Token inputSym;

    private static ControlFlowGraph cfg;
    private static VariableManager vManager;
    private static IntermediateCodeGenerator iCodeGenerator;

    public static Parser getInstance(String fileName)
    {
        if(parser == null)
        {
            parser = new Parser(fileName);

            if(parser.scanner == null)
            {
                return null;
            }
        }

        return parser;
    }

    private Parser(String fileName)
    {
        scanner = Scanner.getInstance(fileName);
    }

    private void next()
    {
        if(scanner != null)
        {
            inputSym = scanner.getSym();
        }
    }

    public ControlFlowGraph parse()
    {
        cfg = ControlFlowGraph.getInstance();
        iCodeGenerator = IntermediateCodeGenerator.getInstance();
        vManager = VariableManager.getInstance();
        next();
        computation();
        return cfg;
    }

    private IResult designator()
    {
        VariableResult vResult = null;
        if(inputSym.isSameType(TokenType.ident))
        {
            if(vManager.isVariable(scanner.identifier2Address.get(inputSym.value)))
            {
                Variable v = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value));
                vResult = new VariableResult();
                vResult.set(v);
            }
            else 
            {
                error(new IllegalVariableException("Undeclared variable found while parsing designator."));
                return vResult;
            }

            next();
            // Array check
            while(inputSym.isSameType(TokenType.openbracketToken))
            {
                // Get dimension info
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Identifier not found while parsing designator."));
        }

        return vResult;
    }

    private IResult factor()
    {
        IResult result = null;
        switch(inputSym.type)
        {
            case ident:
                result = designator();
                if(result != null)
                {
                    Variable variable = ((VariableResult)result).variable;
                    Integer designatorVersion = vManager.getSsaVersion(variable.address);
                    variable.version = designatorVersion;
                    vManager.updateDefUseChain(variable.address, designatorVersion, iCodeGenerator.getPC());
                }
                break;
            
            case number:
                result = new ConstantResult();
                if(result != null)
                {
                    result.set(inputSym.value);
                    next();
                }
                break;
            
            case openparenToken:
                next();
                result = expression();
                if(result != null)
                {
                    if(inputSym.isSameType(TokenType.closeparenToken))
                    {
                        next();
                    }
                    else 
                    {
                        error(new IncorrectSyntaxException("Closing parenthesis not found while parsing factor."));
                    }
                }
                break;

            case callToken:
                result = funcCall();
                break;
        }

        return result;
    }

    private IResult term()
    {
        IResult xResult = factor();
        if(xResult != null)
        {
            while(inputSym.isTermOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = factor();
                if(yResult != null)
                {
                    cfg.current.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }
        }

        return xResult;
    }

    private IResult expression()
    {
        IResult xResult = term();
        if(xResult != null)
        {
            while(inputSym.isExpressionOp()) 
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = term();
                if(yResult != null)
                {
                    cfg.current.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }    
        }

        return xResult;
    }

    private IResult relation()
    {
        IResult xResult = expression();
        if(xResult != null)
        {
            while(inputSym.isRelationOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = expression();
                if(yResult != null)
                {
                    cfg.current.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }
        }

        return xResult;
    }

    private void assignment()
    {
        if(inputSym.isSameType(TokenType.letToken))
        {
            IResult lhsResult = designator();
            if(lhsResult != null)
            {
                if(inputSym.isSameType(TokenType.becomesToken))
                {
                    Token opToken = inputSym;
                    next();

                    IResult rhsResult = expression();
                    if(rhsResult != null)
                    {
                        Variable variable = ((VariableResult)lhsResult).variable;
                        variable.version = iCodeGenerator.getPC();

                        vManager.updateSsaMap(variable.address, variable.version);
                        vManager.updateDefUseChain(variable.address, variable.version, variable.version);
                        cfg.current.addInstruction(iCodeGenerator.Compute(opToken, lhsResult, rhsResult));
                    }
                }
            }
        }
    }

    private IResult funcCall()
    {
        IResult fResult = null;
        if(inputSym.isSameType(TokenType.callToken))
        {
            next();

            
        }

        return fResult;
    }

    private IBlock ifStatement()
    {
        IBlock ifBlock = null;
        if(inputSym.isSameType(TokenType.ifToken))
        {
            next();

            
        }

        return ifBlock;
    }

    private IBlock whileStatement()
    {
        WhileBlock whileBlock = null;
        if(inputSym.isSameType(TokenType.whileToken))
        {
            next();
            whileBlock = (WhileBlock)cfg.initializeWhileBlock(); // Current changes to whileBlock
            IResult result = relation(); // Adds relation inst. to current
            if(result != null)
            {
                if(inputSym.isSameType(TokenType.doToken))
                {
                    next();
                    IBlock loopBlock = statSequence(); // Current changes to loopBlock
                    // Issue: we have to switch to whileBlock to set the phi function and switch back to loopBlock
                    // to add further instructions, but the current implementation does not allow any interactions
                    // What we have to do: we have to at detect the end of a sequence OR make the sequence add phi functions
                    whileBlock.setLoopBlock(loopBlock);
                    loopBlock.setParent(whileBlock);
                    if(inputSym.isSameType(TokenType.odToken))
                    {
                        next();
                        // add branch instr. to loopBlock
                        Integer pc = iCodeGenerator.getPC();
                        Instruction instruction = new Instruction(pc++, OperatorCode.bra, null, null);
                        cfg.current.addInstruction(instruction);

                        // add follow block
                        IBlock followBlock = cfg.initializeBlock(); // Current changes to followBlock
                        whileBlock.setChild(followBlock);
                        followBlock.setParent(whileBlock);
                    }
                }
            }
            
        }

        return (IBlock)whileBlock;
    }

    private IResult returnStatement()
    {
        IResult rResult = null;
        if(inputSym.isSameType(TokenType.returnToken))
        {
            next();

            
        }
        else
        {
            error(new IncorrectSyntaxException("Return token not found while parsing return statement."));
        }

        return rResult;
    }

    private IBlock statement()
    {
        IBlock statementBlock = null;
        

        return statementBlock;
    }

    private IBlock statSequence()
    {
        IBlock statSequenceBlock = null;
        

        return statSequenceBlock;
    }

    private ArrayList<Integer> typeDecl()
    {
        ArrayList<Integer> dimensionList = new ArrayList<Integer>();
        if(inputSym.isSameType(TokenType.varToken))
        {
            next();
        }
        else if(inputSym.isSameType(TokenType.arrToken))
        {
            next();
            // Dimension declaration
            if(inputSym.isSameType(TokenType.openbracketToken))
            {
                next();
                // Get first dimention
                if(inputSym.isSameType(TokenType.number))
                {
                    // Extract number from inputSym
                    dimensionList.add(Integer.parseInt(inputSym.value));
                    next();
                    if(inputSym.isSameType(TokenType.closebracketToken))
                    {
                        next();
                        // Get the remaining dimentions until we hit the end
                        while(inputSym.isSameType(TokenType.openbracketToken))
                        {
                            next(); // Number
                            if(inputSym.isSameType(TokenType.number))
                            {
                                dimensionList.add(Integer.parseInt(inputSym.value));
                            }
                            else
                            {
                                error(new IncorrectSyntaxException("Number not found in array declaration."));
                            }
                            next(); // Close bracket
                            if(!inputSym.isSameType(TokenType.closebracketToken))
                            {
                                error(new IncorrectSyntaxException("Close bracket not found in array declaration."));
                            }
                            next(); // Open bracket
                        }
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Close bracket not found in array declaration."));
                    }
                }
                else
                {
                    error(new IncorrectSyntaxException("Number not found in array declaration."));
                }
            }
            else
            {
                error(new IncorrectSyntaxException("Open bracket not found in array declaration."));
            }
        }
        else 
        {
            error(new IncorrectSyntaxException("Variable/Array declaration not found while parsing type declaration."));
        }
        return dimensionList;
    }

    private void varDecl()
    {
        ArrayList<Integer> dimentionList = new ArrayList<Integer>();
        dimentionList = typeDecl();
        next();
        if(inputSym.isSameType(TokenType.ident))
        {
            if(dimentionList.isEmpty()) // var
            {
                Variable var = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value), iCodeGenerator.getPC());
                vManager.updateSsaMap(var.address, var.version);
                vManager.updateDefUseChain(var.address, var.version, var.version);
            }
            else // array
            {
                ArrayVar var = new ArrayVar(inputSym.value, scanner.identifier2Address.get(inputSym.value), iCodeGenerator.getPC(), dimentionList);
                vManager.updateSsaMap(var.address, var.version);
                vManager.updateDefUseChain(var.address, var.version, var.version);
            }
            try 
            {
                vManager.addGlobalVariable(scanner.identifier2Address.get(inputSym.value));
            }
            catch(Exception e)
            {
                error(e);
            }
            next();
            // Consume the rest of the variables if they exist
            if(inputSym.isSameType(TokenType.commaToken))
            {
                next();
                while(inputSym.isSameType(TokenType.ident))
                {
                    if(dimentionList.isEmpty()) // var
                    {
                        Variable var = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value), iCodeGenerator.getPC());
                        vManager.updateSsaMap(var.address, var.version);
                        vManager.updateDefUseChain(var.address, var.version, var.version);
                    }
                    else // array
                    {
                        ArrayVar var = new ArrayVar(inputSym.value, scanner.identifier2Address.get(inputSym.value), iCodeGenerator.getPC(), dimentionList);
                        vManager.updateSsaMap(var.address, var.version);
                        vManager.updateDefUseChain(var.address, var.version, var.version);
                        try{
                            vManager.addArrayBaseAddress(var.address, var.arraySize);
                        }
                        catch(Exception e)
                        {
                            error(e);
                        }
                    }
                    next();
                    if(inputSym.isSameType(TokenType.commaToken))
                    {
                        next();
                    }
                }
            }
            else if(inputSym.isSameType(TokenType.semiToken))
            {
                next();
            }
            else
            {
                error(new IncorrectSyntaxException("Incorrect Syntax found in Variable Declaration."));
            }
        }
        else
        {
            error(new IncorrectSyntaxException("No Identifier found in Variable Declaration."));
        }
    }

    private void formalParam()
    {
        if(inputSym.isSameType(TokenType.openparenToken))
        {

        }
        else
        {
            error(new IncorrectSyntaxException("Open parenthesis not found while parsing formal parameters declaration."));
        }
    }

    private void funcDecl()
    {
        if(inputSym.isSameType(TokenType.funcToken) || inputSym.isSameType(TokenType.procToken))
        {

        }
        else
        {
            error(new IncorrectSyntaxException("Function or Procedure tokens not found while parsing function declaration."));
        }
    }

    private void funcBody()
    {
        while(inputSym.isSameType(TokenType.varToken) || inputSym.isSameType(TokenType.arrToken)) 
        {
            varDecl();

        }

        if(inputSym.isSameType(TokenType.beginToken))
        {

        }
        else
        {
            error(new IncorrectSyntaxException("Begin token not found while parsing function body."));
        }
    }

    private void computation()
    {
        if(inputSym.isSameType(TokenType.mainToken))
        {
            varDecl();
            funcDecl();

            if(inputSym.isSameType(TokenType.beginToken))
            {
                statSequence();
            }
            else
            {
                error(new IncorrectSyntaxException("Begin token not found while parsing function body."));
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Main token not found while parsing the program."));
        }
    }

    public void error(Exception exception)
    {
        scanner.error(exception);
    }
}
