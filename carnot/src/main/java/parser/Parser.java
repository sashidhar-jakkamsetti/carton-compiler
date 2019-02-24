package parser;

import java.util.*;

import dataStructures.*;
import dataStructures.Blocks.*;
import dataStructures.Instructions.Instruction;
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
        vManager = new VariableManager();
        next();
        computation();
        return cfg;
    }

    private IResult designator(IBlock cBlock, Function function)
    {
        VariableResult vResult = null;
        if(inputSym.isSameType(TokenType.ident))
        {
            if((function == null && vManager.isVariable(scanner.identifier2Address.get(inputSym.value)))
                || (function != null && function.vManager.isVariable(scanner.identifier2Address.get(inputSym.value))))
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
            // TODO: Array check
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

    private IResult factor(IBlock cBlock, Function function)
    {
        IResult result = null;
        switch(inputSym.type)
        {
            case ident:
                result = designator(cBlock, function);
                if(result != null)
                {
                    Variable variable = ((VariableResult)result).variable;
                    if(function == null)
                    {
                        Integer designatorVersion = vManager.getSsaVersion(variable.address);
                        variable.version = designatorVersion;
                        vManager.updateDefUseChain(variable.address, designatorVersion, iCodeGenerator.getPC());
                    }
                    else 
                    {
                        Integer designatorVersion = function.vManager.getSsaVersion(variable.address);
                        variable.version = designatorVersion;
                        function.vManager.updateDefUseChain(variable.address, designatorVersion, iCodeGenerator.getPC());
                    }
                    
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
                result = expression(cBlock, function);
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
                result = funcCall(cBlock, function);
                break;
        }

        return result;
    }

    private IResult term(IBlock cBlock, Function function)
    {
        IResult xResult = factor(cBlock, function);
        if(xResult != null)
        {
            while(inputSym.isTermOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = factor(cBlock, function);
                if(yResult != null)
                {
                    xResult.setIid(iCodeGenerator.getPC());
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }
        }

        return xResult;
    }

    private IResult expression(IBlock cBlock, Function function)
    {
        IResult xResult = term(cBlock, function);
        if(xResult != null)
        {
            while(inputSym.isExpressionOp()) 
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = term(cBlock, function);
                if(yResult != null)
                {
                    xResult.setIid(iCodeGenerator.getPC());
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }    
        }

        return xResult;
    }

    private BranchResult relation(IBlock cBlock, Function function)
    {
        BranchResult bResult = new BranchResult();
        IResult xResult = expression(cBlock, function);
        if(xResult != null)
        {
            while(inputSym.isRelationOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = expression(cBlock, function);
                if(yResult != null)
                {
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                    bResult.condition = opToken;
                    bResult.fixuplocation = iCodeGenerator.getPC();
                    bResult.iid = iCodeGenerator.getPC() - 1;
                    bResult.targetBlock = cBlock;
                }
            }
        }

        return bResult;
    }

    private void assignment(IBlock cBlock, Function function)
    {
        if(inputSym.isSameType(TokenType.letToken))
        {
            IResult lhsResult = designator(cBlock, function);
            if(lhsResult != null)
            {
                if(inputSym.isSameType(TokenType.becomesToken))
                {
                    Token opToken = inputSym;
                    next();

                    IResult rhsResult = expression(cBlock, function);
                    if(rhsResult != null)
                    {
                        if(rhsResult.getIid() > 0) 
                        {
                            rhsResult = rhsResult.toInstruction();
                        }
                        Variable variable = ((VariableResult)lhsResult).variable;
                        variable.version = iCodeGenerator.getPC();

                        if(function == null)
                        {
                            vManager.updateSsaMap(variable.address, variable.version);
                            vManager.updateDefUseChain(variable.address, variable.version, variable.version);
                        }
                        else 
                        {
                            function.vManager.updateSsaMap(variable.address, variable.version);
                            function.vManager.updateDefUseChain(variable.address, variable.version, variable.version);
                        }
                        cBlock.addInstruction(iCodeGenerator.Compute(opToken, lhsResult, rhsResult));
                    }
                }
            }
        }
    }

    private IResult funcCall(IBlock cBlock, Function function)
    {
        if(inputSym.isSameType(TokenType.callToken))
        {
            Token opToken = inputSym;
            next();
            Function callFunction = new Function(inputSym.value, scanner.identifier2Address.get(inputSym.value));
            if(cfg.isExists(callFunction))
            {
                callFunction = cfg.getFunction(callFunction);
                next();
                if(inputSym.isSameType(TokenType.openparenToken))
                {
                    Integer idx = 0;
                    do
                    {
                        next();
                        IResult pResult = expression(cBlock, function);
                        if(pResult != null)
                        {
                            cBlock.addInstruction(iCodeGenerator.Compute(OperatorCode.move, pResult, callFunction.getParameter(idx++)));
                        }
                    }while(inputSym.isSameType(TokenType.commaToken));
                    
                    if(inputSym.isSameType(TokenType.closeparenToken))
                    {
                        next();
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Closing parenthesis not found while parsing funcCall statement."));
                    }
                }

                BranchResult bResult = new BranchResult();
                bResult.set(iCodeGenerator.getPC());
                bResult.set(callFunction.head);
                bResult.condition = opToken;
                
                cBlock.addInstruction(iCodeGenerator.Compute(opToken, null, bResult));
                if(function == null)
                {
                    cBlock.setSsaMap(vManager.getSsaMap());
                }
                else 
                {
                    cBlock.setSsaMap(function.vManager.getSsaMap());
                }
                return callFunction.returnInstruction;
            }

            if(Operator.standardIoOperator.containsKey(inputSym.value))
            {
                opToken = inputSym;
                if(inputSym.value == "InputNum")
                {
                    next();
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, null, null));
                    InstructionResult iResult = new InstructionResult();
                    iResult.setIid(iCodeGenerator.getPC() - 1);
                    return iResult;
                }
                else if(inputSym.value == "OutputNum")
                {
                    next();
                    if(inputSym.isSameType(TokenType.openparenToken))
                    {
                        next();
                        IResult pResult = expression(cBlock, function);
                        if(pResult != null)
                        {
                            cBlock.addInstruction(iCodeGenerator.Compute(opToken, pResult, null));
                        }
                        if(inputSym.isSameType(TokenType.closeparenToken))
                        {
                            next();
                            return null;
                        }
                        else
                        {
                            error(new IncorrectSyntaxException("Closing parenthesis not found while parsing funcCall statement."));
                        }
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Open parenthesis not found while parsing funcCall statement."));
                    }
                }
                else
                {
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, null, null));
                    return null;
                }
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Call token not found while parsing funcCall statement."));
        }

        return null;
    }

    private IBlock ifStatement(IBlock cBlock, Function function)
    {
        JoinBlock jBlock = null;
        if(inputSym.isSameType(TokenType.ifToken))
        {
            next();
            IfBlock iBlock = cfg.initializeIfBlock();
            iBlock.setParent(cBlock);
            cBlock.setChild(iBlock);

            jBlock = cfg.initializeJoinBlock();
            iBlock.setChild(jBlock);
            jBlock.setParent(iBlock);

            BranchResult bResult = relation(iBlock, function);
            iBlock.addInstruction(iCodeGenerator.Compute(bResult.condition, bResult, null));
            
            if(function == null)
            {
                cBlock.setSsaMap(vManager.getSsaMap());
                iBlock.setSsaMap(vManager.getSsaMap());
            }
            else 
            {
                cBlock.setSsaMap(function.vManager.getSsaMap());
                iBlock.setSsaMap(function.vManager.getSsaMap());
            }

            if(inputSym.isSameType(TokenType.thenToken))
            {
                BranchResult bResult2 = new BranchResult();
                bResult2.condition = inputSym;

                next();
                Block tBlock = cfg.initializeBlock();
                iBlock.setThenBlock(tBlock);
                tBlock.setParent(iBlock);

                HashMap<Integer, Integer> restoreSsa = new HashMap<Integer, Integer>();
                if(function == null)
                {
                    vManager.copySsaTo(restoreSsa);
                }
                else
                {
                    function.vManager.copySsaTo(restoreSsa);
                }

                tBlock = (Block)statSequence(tBlock, function);
                bResult2.set(jBlock);
                tBlock.addInstruction(iCodeGenerator.Compute(bResult2.condition, null, bResult2));
                tBlock.setChild(jBlock);
                jBlock.setThenBlock(tBlock);
                
                if(function == null)
                {
                    tBlock.setSsaMap(vManager.getSsaMap());
                }
                else 
                {
                    tBlock.setSsaMap(function.vManager.getSsaMap());
                }

                if(inputSym.isSameType(TokenType.elseToken))
                {
                    next();
                    Block eBlock = cfg.initializeBlock();
                    iBlock.setElseBlock(eBlock);
                    eBlock.setParent(iBlock);

                    if(function == null)
                    {
                        vManager.setSsaMap(restoreSsa);
                    }
                    else
                    {
                        function.vManager.setSsaMap(restoreSsa);
                    } 
                    
                    eBlock = (Block)statSequence(eBlock, function);
                    iBlock.fixupBranch(bResult.fixuplocation, eBlock);
                    eBlock.setChild(jBlock);
                    jBlock.setElseBlock(eBlock);
                    
                    if(function == null)
                    {
                        eBlock.setSsaMap(vManager.getSsaMap());
                    }
                    else 
                    {
                        eBlock.setSsaMap(function.vManager.getSsaMap());
                    }
                }
                else
                {
                    iBlock.fixupBranch(bResult.fixuplocation, jBlock);
                }

                if(inputSym.isSameType(TokenType.fiToken))
                {
                    next();
                    jBlock.createPhis(scanner.address2Identifier);

                    if(function == null)
                    {
                        vManager.setSsaMap(jBlock.getSsaMap());
                    }
                    else
                    {
                        function.vManager.setSsaMap(jBlock.getSsaMap());
                    }
                }
                else
                {
                    error(new IncorrectSyntaxException("Fi token not found while parsing if statement."));
                }
            }
            else
            {
                error(new IncorrectSyntaxException("Then token not found while parsing if statement."));
            }
        }
        else 
        {
            error(new IncorrectSyntaxException("If token not found while parsing if statement."));
        }

        return jBlock;
    }

    private IBlock whileStatement(IBlock cBlock, Function function)
    {
        WhileBlock whileBlock = null;

        return (IBlock)whileBlock;
    }

    private IResult returnStatement(IBlock cBlock, Function function)
    {
        IResult rResult = null;
        if(inputSym.isSameType(TokenType.returnToken))
        {
            Token opToken = inputSym;
            next();
            rResult = expression(cBlock, function);
            if(rResult != null)
            {
                InstructionResult iResult = new InstructionResult();
                iResult.set(iCodeGenerator.getPC());
                function.returnInstruction = iResult;
                cBlock.addInstruction(iCodeGenerator.Compute(opToken, rResult, iResult));
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Return token not found while parsing return statement."));
        }

        return rResult;
    }

    private IBlock statement(IBlock cBlock, Function function)
    {
        IBlock block = null;
        if(inputSym.isSameType(TokenType.letToken))
        {
            assignment(cBlock, function);
            block = cBlock;
        }
        else if(inputSym.isSameType(TokenType.callToken))
        {
            funcCall(cBlock, function);
            block = cBlock;
        }
        else if(inputSym.isSameType(TokenType.ifToken))
        {
            block = ifStatement(cBlock, function);
        }
        else if(inputSym.isSameType(TokenType.whileToken))
        {
            block = whileStatement(cBlock, function);
        }
        else if(inputSym.isSameType(TokenType.returnToken))
        {
            returnStatement(cBlock, function);
            block = cBlock;
        }
        else
        {
            error(new IncorrectSyntaxException("No valid token found while parsing statement."));
        }

        return block;
    }

    private IBlock statSequence(IBlock cBlock, Function function)
    {
        IBlock block = null;
        do
        {
            block = statement(cBlock, function);
            if(inputSym.isSameType(TokenType.semiToken))
            {
                next();
            }
            else
            {
                break;
            }
        }while(true);
        
        return block;
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

    private void varDecl(Function function)
    {
        ArrayList<Integer> dimentionList = new ArrayList<Integer>();
        dimentionList = typeDecl();
        do
        {
            next();
            if(inputSym.isSameType(TokenType.ident))
            {
                next();
                VariableResult vResult = new VariableResult();
                if(dimentionList.isEmpty()) // var
                {
                    Variable var = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value), iCodeGenerator.getPC());
                    vResult.set(var);
                }
                else // array
                {
                    ArrayVar var = new ArrayVar(inputSym.value, scanner.identifier2Address.get(inputSym.value), iCodeGenerator.getPC(), dimentionList);
                    vResult.set(var);
                }
                                
                try 
                {
                    if(function == null)
                    {
                        iCodeGenerator.declareVariable(cfg.head, vManager, vResult);
                    }
                    else
                    {
                        iCodeGenerator.declareVariable(function.head, function.vManager, vResult);
                    }
                }
                catch(Exception e)
                {
                    error(e);
                }
            }
            else
            {
                error(new IncorrectSyntaxException("No Identifier found in Variable Declaration."));
            }
            
        }while(inputSym.isSameType(TokenType.commaToken));

        if(inputSym.isSameType(TokenType.semiToken))
        {
            next();
        }
        else
        {
            error(new IncorrectSyntaxException("Semi comma not found in Variable Declaration."));
        }
    }

    private void formalParam(Function function)
    {
        if(inputSym.isSameType(TokenType.openparenToken))
        {
            next();
            while(inputSym.isSameType(TokenType.ident))
            {
                Variable v = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value));
                VariableResult vResult = new VariableResult();
                vResult.set(v);
                vResult.setIid(iCodeGenerator.getPC());
                try
                {
                    iCodeGenerator.declareVariable(function.head, function.vManager, vResult);
                }
                catch(Exception e)
                {
                    error(e);
                }
                
                function.addParameter(vResult);
                next();

                if(inputSym.isSameType(TokenType.commaToken))
                {
                    next();
                    if(!inputSym.isSameType(TokenType.ident))
                    {
                        error(new IncorrectSyntaxException("Identifier not found while parsing formal paramters declaration."));
                    }
                }
                else
                {
                    break;
                }
            }

            if(inputSym.isSameType(TokenType.closeparenToken))
            {
                next();
            }
            else
            {
                error(new IncorrectSyntaxException("Close parenthesis not found while parsing formal parameters declaration."));
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Open parenthesis not found while parsing formal parameters declaration."));
        }
    }

    private void funcDecl()
    {
        while(inputSym.isSameType(TokenType.funcToken) || inputSym.isSameType(TokenType.procToken))
        {
            next();
            if(inputSym.isSameType(TokenType.ident))
            {
                Function function = new Function(inputSym.value, scanner.identifier2Address.get(inputSym.value));
                if(cfg.isExists(function))
                {
                    error(new IncorrectSyntaxException("Function already exists."));
                }
                function.head = (Block)cfg.initializeBlock();
                function.setGlobalVariables(vManager.getVariables());
                formalParam(function);

                if(inputSym.isSameType(TokenType.semiToken))
                {
                    next();
                    funcBody(function);

                    if(inputSym.isSameType(TokenType.semiToken))
                    {
                        next();
                    }
                    else 
                    {
                        error(new IncorrectSyntaxException("Semi comma not found while parsing function declaration."));
                    }
                }
                else
                {
                    error(new IncorrectSyntaxException("Semi comma not found while parsing function declaration."));
                }
            }
        }
    }

    private void funcBody(Function function)
    {
        varDecl(function);

        if(inputSym.isSameType(TokenType.beginToken))
        {
            statSequence(function.head, function);
            if(inputSym.isSameType(TokenType.endToken))
            {
                next();
            }
            else 
            {
                error(new IncorrectSyntaxException("End token not found while parsing function body."));
            }
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
            next();
            varDecl(null);
            funcDecl();

            if(inputSym.isSameType(TokenType.beginToken))
            {
                IBlock lBlock = statSequence(cfg.head, null);
                if(inputSym.isSameType(TokenType.endToken))
                {
                    next();
                    if(inputSym.isSameType(TokenType.periodToken))
                    {
                        Token opToken = inputSym;
                        next();
                        lBlock.addInstruction(iCodeGenerator.Compute(opToken, null, null));
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Period token not found while parsing main function body."));
                    }
                }
                else 
                {
                    error(new IncorrectSyntaxException("End token not found while parsing main function body."));
                }
            }
            else
            {
                error(new IncorrectSyntaxException("Begin token not found while parsing main function body."));
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
