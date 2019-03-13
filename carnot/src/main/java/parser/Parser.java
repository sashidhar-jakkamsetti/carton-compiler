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
import utility.Constants;

public class Parser 
{
    private static Parser parser;
    private Scanner scanner;
    private Token inputSym;

    private ControlFlowGraph cfg;
    private VariableManager vManager;
    private static IntermediateCodeGenerator iCodeGenerator;
    private static String cfileName;

    public static Parser getInstance(String fileName)
    {
        if(parser == null || cfileName != fileName)
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
        cfg = new ControlFlowGraph();
        iCodeGenerator = IntermediateCodeGenerator.getInstance();
        iCodeGenerator.reset();
        vManager = cfg.mVariableManager;
        next();
        cfg.done = computation(true);
        return cfg;
    }

    private VariableResult designator(IBlock cBlock, Function function, Boolean optimize)
    {
        VariableResult vResult = new VariableResult();
        if(inputSym.isSameType(TokenType.ident))
        {
            Integer variable = scanner.identifier2Address.get(inputSym.value);
            Variable v = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value));

            if(vManager.isVariable(variable) || (function != null && function.vManager.isVariable(variable)))
            {
                next();
                if(vManager.isArray(variable) || (function != null && function.vManager.isArray(variable)))
                {
                    if(inputSym.isSameType(TokenType.openbracketToken))
                    {
                        ArrayList<IResult> indexList = new ArrayList<IResult>();
                        do
                        {
                            if(inputSym.isSameType(TokenType.openbracketToken))
                            {
                                next();
                                indexList.add(expression(cBlock, function, optimize));
                                if(inputSym.isSameType(TokenType.closebracketToken))
                                {
                                    next();
                                }
                                else
                                {
                                    error(new IncorrectSyntaxException("Close bracket not found in array declaration."));
                                    return null;
                                }
                            }
                            else
                            {
                                error(new IncorrectSyntaxException("Open bracket not found in array declaration."));
                                return null;
                            }
                        }while(inputSym.isSameType(TokenType.openbracketToken));
        
                        ArrayVar arrayV = new ArrayVar(v.name, v.address, v.version);
                        arrayV.indexList = indexList;
                        vResult.set(arrayV);
                    }
                    else 
                    {
                        error(new IllegalVariableException("Undeclared array found while parsing designator."));
                        return null;
                    }
                }
                else
                {
                    vResult.set(v);
                }
            }
            else 
            {
                error(new IllegalVariableException("Undeclared variable found while parsing designator."));
                return null;
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Identifier not found while parsing designator."));
            return null;
        }

        return vResult;
    }

    private IResult factor(IBlock cBlock, Function function, Boolean optimize)
    {
        IResult result = null;
        switch(inputSym.type)
        {
            case ident:
                result = designator(cBlock, function, optimize);
                if(result != null)
                {
                    Variable v = ((VariableResult)result).variable;
                    if(vManager.isVariable(v.address))
                    {
                        Integer designatorVersion = vManager.getSsaVersion(v.address);
                        v.version = designatorVersion;
                        vManager.updateDefUseChain(v.address, designatorVersion, iCodeGenerator.getPC());

                        if(((VariableResult)result).isArray)
                        {
                            iCodeGenerator.loadArrayElement(cBlock, vManager, result, optimize);
                            result = result.toInstruction();
                            result.set(iCodeGenerator.getPC() - 1);
                        }
                    }
                    else if(function != null && function.vManager.isVariable(v.address))
                    {
                        Integer designatorVersion = function.vManager.getSsaVersion(v.address);
                        v.version = designatorVersion;
                        function.vManager.updateDefUseChain(v.address, designatorVersion, iCodeGenerator.getPC());

                        if(((VariableResult)result).isArray)
                        {
                            iCodeGenerator.loadArrayElement(cBlock, function.vManager, result, optimize);
                            result = result.toInstruction();
                            result.set(iCodeGenerator.getPC() - 1);
                        }
                    }
                }
                break;
            
            case number:
                result = new ConstantResult();
                result.set(inputSym.value);
                next();
                break;
            
            case openparenToken:
                next();
                result = expression(cBlock, function, optimize);
                if(result != null)
                {
                    if(inputSym.isSameType(TokenType.closeparenToken))
                    {
                        result = result.toInstruction();
                        next();
                    }
                    else 
                    {
                        error(new IncorrectSyntaxException("Closing parenthesis not found while parsing factor."));
                    }
                }
                break;

            case callToken:
                result = funcCall(cBlock, function, optimize);
                break;
        }

        if(result != null)
        {
            return result.clone();
        }
        return result;
    }

    private IResult term(IBlock cBlock, Function function, Boolean optimize)
    {
        IResult xResult = factor(cBlock, function, optimize);
        if(xResult != null)
        {
            while(inputSym.isTermOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = factor(cBlock, function, optimize);
                if(yResult != null)
                {
                    if(xResult.getIid() > 0) 
                    {
                        xResult = xResult.toInstruction();
                    }
                    iCodeGenerator.compute(cBlock, opToken, xResult, yResult, optimize);
                    xResult = xResult.clone();
                    xResult.setIid(iCodeGenerator.getPC() - 1);
                }
            }
        }

        return xResult;
    }

    private IResult expression(IBlock cBlock, Function function, Boolean optimize)
    {
        IResult xResult = term(cBlock, function, optimize);
        if(xResult != null)
        {
            while(inputSym.isExpressionOp()) 
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = term(cBlock, function, optimize);
                if(yResult != null)
                {
                    if(xResult.getIid() > 0) 
                    {
                        xResult = xResult.toInstruction();
                    }

                    iCodeGenerator.compute(cBlock, opToken, xResult, yResult, optimize);
                    xResult = xResult.clone();
                    xResult.setIid(iCodeGenerator.getPC() - 1);
                }
            }    
        }

        return xResult;
    }

    private BranchResult relation(IBlock cBlock, Function function, Boolean optimize)
    {
        BranchResult bResult = new BranchResult();
        IResult xResult = expression(cBlock, function, optimize);
        if(xResult != null)
        {
            if(xResult.getIid() > 0) 
            {
                xResult = xResult.toInstruction();
                xResult.setIid(iCodeGenerator.getPC() - 1);
            }
            while(inputSym.isRelationOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = expression(cBlock, function, optimize);
                if(yResult != null)
                {
                    if(yResult.getIid() > 0) 
                    {
                        yResult = yResult.toInstruction();
                        yResult.setIid(iCodeGenerator.getPC() - 1);
                    }
                    iCodeGenerator.compute(cBlock, opToken, xResult, yResult, optimize);
                    bResult.condition = opToken;
                    bResult.fixuplocation = iCodeGenerator.getPC();
                    bResult.iid = iCodeGenerator.getPC() - 1;
                    bResult.targetBlock = cBlock;
                }
            }
        }

        return (BranchResult)bResult.clone();
    }

    private void assignment(IBlock cBlock, Function function, Boolean optimize, ArrayList<Instruction> kill)
    {
        if(inputSym.isSameType(TokenType.letToken))
        {
            next();
            IResult lhsResult = designator(cBlock, function, optimize);
            if(lhsResult != null)
            {
                if(inputSym.isSameType(TokenType.becomesToken))
                {
                    Token opToken = inputSym;
                    next();

                    IResult rhsResult = expression(cBlock, function, optimize);
                    if(rhsResult != null)
                    {
                        if(rhsResult.getIid() > 0) 
                        {
                            rhsResult = rhsResult.toInstruction();
                            // rhsResult.set(iCodeGenerator.getPC() - 1); // Fishy?
                        }

                        Variable v = ((VariableResult)lhsResult).variable;
                        if(vManager.isVariable(v.address))
                        {
                            if(((VariableResult)lhsResult).isArray)
                            {
                                iCodeGenerator.storeArrayElement(cBlock, vManager, lhsResult, rhsResult, optimize);
                                Instruction newKill = new Instruction(0);
                                newKill.setExternal(0, OperatorCode.store, lhsResult, null);
                                kill.add(newKill);
                                lhsResult = lhsResult.toInstruction();
                                lhsResult.set(iCodeGenerator.getPC() - 1);
                            }
                            else
                            {
                                v.version = iCodeGenerator.getPC();
                                iCodeGenerator.compute(cBlock, opToken, lhsResult, rhsResult, optimize);
                                vManager.updateSsaMap(v.address, v.version);
                                vManager.updateDefUseChain(v.address, v.version, v.version);
                            }
                        }
                        else if(function != null && function.vManager.isVariable(v.address))
                        {
                            if(((VariableResult)lhsResult).isArray)
                            {
                                iCodeGenerator.storeArrayElement(cBlock, function.vManager, lhsResult, rhsResult, optimize);
                                Instruction newKill = new Instruction(0);
                                newKill.setExternal(0, OperatorCode.store, lhsResult, null);
                                kill.add(newKill);
                                lhsResult = lhsResult.toInstruction();
                                lhsResult.set(iCodeGenerator.getPC() - 1);
                            }
                            else
                            {
                                v.version = iCodeGenerator.getPC();
                                iCodeGenerator.compute(cBlock, opToken, lhsResult, rhsResult, optimize);
                                function.vManager.updateSsaMap(v.address, v.version);
                                function.vManager.updateDefUseChain(v.address, v.version, v.version);
                            }
                        }
                    }
                }
            }
        }
    }

    private IResult funcCall(IBlock cBlock, Function function, Boolean optimize)
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
                        IResult pResult = expression(cBlock, function, optimize);
                        if(pResult != null)
                        {
                            iCodeGenerator.compute(cBlock, OperatorCode.move, callFunction.getParameter(idx++), pResult, optimize);
                        }
                    }while(inputSym.isSameType(TokenType.commaToken));
                    
                    if(inputSym.isSameType(TokenType.closeparenToken))
                    {
                        next();
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Closing parenthesis not found while parsing funcCall statement."));
                        return null;
                    }
                }

                BranchResult bResult = new BranchResult();
                bResult.set(iCodeGenerator.getPC());
                bResult.set(callFunction.head);
                bResult.condition = opToken;
                
                iCodeGenerator.compute(cBlock, opToken, bResult, optimize);
                if(callFunction.returnInstruction != null)
                {
                    return callFunction.returnInstruction.clone();
                }
                return callFunction.returnInstruction;
            }

            if(Operator.standardIoOperator.containsKey(inputSym.value))
            {
                opToken = inputSym;
                if(inputSym.value.equals("InputNum"))
                {
                    next();
                    if(inputSym.isSameType(TokenType.openparenToken))
                    {
                        next();
                        if(inputSym.isSameType(TokenType.closeparenToken))
                        {
                            next();
                        }
                        else
                        {
                            error(new IncorrectSyntaxException("Close parenthesis not found while parsing InputNum statement."));
                            return null;
                        }
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Open parenthesis not found while parsing InputNum statement."));
                        return null;
                    }
                    iCodeGenerator.compute(cBlock, opToken, null, null, optimize);
                    return new InstructionResult(iCodeGenerator.getPC() - 1);
                }
                else if(inputSym.value.equals("OutputNum"))
                {
                    next();
                    if(inputSym.isSameType(TokenType.openparenToken))
                    {
                        next();
                        IResult pResult = expression(cBlock, function, optimize);
                        if(pResult != null)
                        {
                            iCodeGenerator.compute(cBlock, opToken, pResult, null, optimize);
                        }
                        if(inputSym.isSameType(TokenType.closeparenToken))
                        {
                            next();
                            return null;
                        }
                        else
                        {
                            error(new IncorrectSyntaxException("Closing parenthesis not found while parsing OutputNum statement."));
                            return null;
                        }
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Open parenthesis not found while parsing OutputNum statement."));
                        return null;
                    }
                }
                else
                {
                    next();
                    if(inputSym.isSameType(TokenType.openparenToken))
                    {
                        next();
                        if(inputSym.isSameType(TokenType.closeparenToken))
                        {
                            next();
                        }
                        else
                        {
                            error(new IncorrectSyntaxException("Close parenthesis not found while parsing InputNum statement."));
                            return null;
                        }
                    }
                    else
                    {
                        error(new IncorrectSyntaxException("Open parenthesis not found while parsing InputNum statement."));
                        return null;
                    }
                    iCodeGenerator.compute(cBlock, opToken, null, null, optimize);
                    return null;
                }
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Call token not found while parsing funcCall statement."));
            return null;
        }

        return null;
    }

    private IBlock ifStatement(IBlock cBlock, Function function, Boolean optimize, ArrayList<Instruction> kill)
    {
        JoinBlock jBlock = null;
        if(inputSym.isSameType(TokenType.ifToken))
        {
            next();
            IfBlock iBlock = cfg.initializeIfBlock();
            iBlock.setParent(cBlock);
            cBlock.setChild(iBlock);

            jBlock = cfg.initializeJoinBlock();
            iBlock.setJoinBlock(jBlock);
            jBlock.setParent(iBlock);

            BranchResult bResult = relation(iBlock, function, optimize);
            iCodeGenerator.compute(iBlock, bResult.condition, bResult, optimize);

            if(function == null)
            {
                cBlock.freezeSsa(vManager.getSsaMap(), null);
                iBlock.freezeSsa(vManager.getSsaMap(), null);
            }
            else 
            {
                cBlock.freezeSsa(vManager.getSsaMap(), function.vManager.getSsaMap());
                iBlock.freezeSsa(vManager.getSsaMap(), function.vManager.getSsaMap());
            }

            if(inputSym.isSameType(TokenType.thenToken))
            {
                BranchResult bResult2 = new BranchResult();
                bResult2.condition = inputSym;

                next();
                Block tBlock = cfg.initializeBlock();
                iBlock.setThenBlock(tBlock);
                tBlock.setParent(iBlock);

                tBlock = (Block)statSequence(tBlock, function, optimize, kill);
                if(tBlock == null)
                {
                    return null;
                }
                bResult2.set(jBlock);
                iCodeGenerator.compute(tBlock, bResult2.condition, bResult2, optimize);
                tBlock.setChild(jBlock);
                jBlock.setThenBlock(tBlock);

                if(kill.stream().anyMatch(k -> k.id.equals(0)))
                {
                    jBlock.addKill(kill);
                }
                
                if(function == null)
                {
                    tBlock.freezeSsa(vManager.getSsaMap(), null);
                }
                else 
                {
                    tBlock.freezeSsa(vManager.getSsaMap(), function.vManager.getSsaMap());
                }

                if(inputSym.isSameType(TokenType.elseToken))
                {
                    next();
                    Block eBlock = cfg.initializeBlock();
                    iBlock.setElseBlock(eBlock);
                    eBlock.setParent(iBlock);
                    iBlock.fixupBranch(bResult.fixuplocation, eBlock);

                    vManager.setSsaMap(cBlock.getGlobalSsa());
                    if(function != null)
                    {
                        function.vManager.setSsaMap(cBlock.getLocalSsa());
                    }
                    
                    eBlock = (Block)statSequence(eBlock, function, optimize, kill);
                    if(eBlock == null)
                    {
                        return null;
                    }
                    eBlock.setChild(jBlock);
                    jBlock.setElseBlock(eBlock);

                    if(kill.stream().anyMatch(k -> k.id.equals(0)))
                    {
                        jBlock.addKill(kill);
                    }
                    
                    if(function == null)
                    {
                        eBlock.freezeSsa(vManager.getSsaMap(), null);
                    }
                    else 
                    {
                        eBlock.freezeSsa(vManager.getSsaMap(), function.vManager.getSsaMap());
                    }
                }
                else
                {
                    iBlock.fixupBranch(bResult.fixuplocation, jBlock);
                }

                if(inputSym.isSameType(TokenType.fiToken))
                {
                    next();
                    jBlock.createPhis(scanner.address2Identifier, iCodeGenerator, optimize);

                    if(function == null)
                    {
                        jBlock.updateIncomingVManager(vManager, null);

                    }
                    else
                    {
                        jBlock.updateIncomingVManager(vManager, function.vManager);
                    }
                }
                else
                {
                    error(new IncorrectSyntaxException("Fi token not found while parsing if statement."));
                    return null;
                }
            }
            else
            {
                error(new IncorrectSyntaxException("Then token not found while parsing if statement."));
                return null;
            }
        }
        else 
        {
            error(new IncorrectSyntaxException("If token not found while parsing if statement."));
            return null;
        }

        return jBlock;
    }

    private IBlock whileStatement(IBlock cBlock, Function function, Boolean optimizeOut, ArrayList<Instruction> kill)
    {
        IBlock fBlock = null;
        Boolean optimizeIn = false;

        if(inputSym.isSameType(TokenType.whileToken))
        {
            next();
            WhileBlock wBlock = cfg.initializeWhileBlock();
            wBlock.setParent(cBlock);
            cBlock.setChild(wBlock);

            IBlock lBlock = cfg.initializeBlock();
            wBlock.setLoopBlock(lBlock);
            lBlock.setParent(wBlock);

            BranchResult bResult = relation(wBlock, function, optimizeIn);
            iCodeGenerator.compute(wBlock, bResult.condition, bResult, optimizeIn);
            
            if(function == null)
            {
                cBlock.freezeSsa(vManager.getSsaMap(), null);
                wBlock.freezeSsa(vManager.getSsaMap(), null);
            }
            else 
            {
                cBlock.freezeSsa(vManager.getSsaMap(), function.vManager.getSsaMap());
                wBlock.freezeSsa(vManager.getSsaMap(), function.vManager.getSsaMap());
            }

            if(inputSym.isSameType(TokenType.doToken))
            {
                BranchResult bResult2 = new BranchResult();
                bResult2.condition = inputSym;
                bResult2.set(wBlock);
                next();

                lBlock = statSequence(lBlock, function, optimizeIn, kill);
                if(lBlock == null)
                {
                    return null;
                }
                if(inputSym.isSameType(TokenType.odToken))
                {
                    next();
                    bResult2.set(wBlock);
                    iCodeGenerator.compute(lBlock, bResult2.condition, bResult2, optimizeIn);
                    lBlock.setChild(wBlock);
                    wBlock.setChild(lBlock); // Bad name!

                    if(kill.stream().anyMatch(k -> k.id.equals(0)))
                    {
                        wBlock.addKill(kill);
                    }
            
                    if(function == null)
                    {
                        lBlock.freezeSsa(vManager.getSsaMap(), null);
                    }
                    else 
                    {
                        lBlock.freezeSsa(vManager.getSsaMap(), function.vManager.getSsaMap());
                    }

                    wBlock.createPhis(lBlock, scanner.address2Identifier, iCodeGenerator, optimizeIn);
                    if(function == null)
                    {
                        wBlock.updateIncomingVManager(vManager, null);
                    }
                    else
                    {
                        wBlock.updateIncomingVManager(vManager, function.vManager);
                    }

                    wBlock.updatePhiVarOccurances(optimizeOut);
                    wBlock.optimizeWhilePhis(optimizeOut);
                    fBlock = cfg.initializeBlock();
                    fBlock.setParent(wBlock);
                    wBlock.setFollowBlock(fBlock);
                    wBlock.fixupBranch(bResult.fixuplocation, fBlock);
                }
                else
                {
                    error(new IncorrectSyntaxException("Od token not found while parsing while statement."));
                    return null;
                }
            }
            else
            {
                error(new IncorrectSyntaxException("Do token not found while parsing while statement."));
                return null;
            }
        }
        else
        {
            error(new IncorrectSyntaxException("While token not found while parsing while statement."));
            return null;
        }

        return fBlock;
    }

    private IResult returnStatement(IBlock cBlock, Function function, Boolean optimize)
    {
        IResult rResult = null;
        if(inputSym.isSameType(TokenType.returnToken))
        {
            Token opToken = inputSym;
            next();
            rResult = expression(cBlock, function, optimize);
            if(rResult != null)
            {
                InstructionResult iResult = new InstructionResult(iCodeGenerator.getPC());
                if(function.returnInstruction == null || function.returnInstruction.iid == -1)
                {
                    function.returnInstruction = iResult;
                }
                else
                {
                    iResult.set(function.returnInstruction.iid);
                }
                if(rResult.getIid() > 0)
                {
                    rResult = rResult.toInstruction();
                }
                iCodeGenerator.compute(cBlock, opToken, iResult, rResult, optimize);
                rResult = rResult.clone();
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Return token not found while parsing return statement."));
            return null;
        }

        return rResult;
    }

    private IBlock statement(IBlock cBlock, Function function, Boolean optimize, ArrayList<Instruction> kill)
    {
        IBlock block = null;
        if(inputSym.isSameType(TokenType.letToken))
        {
            assignment(cBlock, function, optimize, kill);
            block = cBlock;
        }
        else if(inputSym.isSameType(TokenType.callToken))
        {
            funcCall(cBlock, function, optimize);
            block = cBlock;
        }
        else if(inputSym.isSameType(TokenType.ifToken))
        {
            block = ifStatement(cBlock, function, optimize, kill);
        }
        else if(inputSym.isSameType(TokenType.whileToken))
        {
            block = whileStatement(cBlock, function, optimize, kill);
        }
        else if(inputSym.isSameType(TokenType.returnToken))
        {
            returnStatement(cBlock, function, optimize);
            block = cBlock;
        }
        else
        {
            error(new IncorrectSyntaxException("No valid token found while parsing statement."));
        }

        return block;
    }

    private IBlock statSequence(IBlock cBlock, Function function, Boolean optimize, ArrayList<Instruction> kill)
    {
        IBlock block = cBlock;
        do
        {
            block = statement(block, function, optimize, kill);
            if(block == null)
            {
                return null;
            }

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
            do
            {
                if(inputSym.isSameType(TokenType.openbracketToken))
                {
                    next();
                    if(inputSym.isSameType(TokenType.number))
                    {
                        dimensionList.add(Integer.parseInt(inputSym.value));
                        next();
                        if(inputSym.isSameType(TokenType.closebracketToken))
                        {
                            next();
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
            }while(inputSym.isSameType(TokenType.openbracketToken));
        }
        else 
        {
            error(new IncorrectSyntaxException("Variable/Array declaration not found while parsing type declaration."));
        }
        return dimensionList;
    }

    private void varDecl(Function function, Boolean optimize)
    {
        ArrayList<Integer> dimentionList = new ArrayList<Integer>();
        dimentionList = typeDecl();
        do
        {
            if(inputSym.isSameType(TokenType.ident))
            {
                VariableResult vResult = new VariableResult();
                if(dimentionList.isEmpty())
                {
                    Variable var = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value), iCodeGenerator.getPC());
                    vResult.set(var);
                }
                else
                {
                    ArrayVar var = new ArrayVar(inputSym.value, scanner.identifier2Address.get(inputSym.value), 
                                                        iCodeGenerator.getPC(), dimentionList);
                    vResult.set(var);
                }
                                
                try 
                {
                    if(function == null)
                    {
                        iCodeGenerator.declareVariable(cfg.head, vManager, vResult, true, optimize);
                    }
                    else
                    {
                        iCodeGenerator.declareVariable(function.head, function.vManager, vResult, true, optimize);
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
                return;
            }

            next();
            if(inputSym.isSameType(TokenType.commaToken))
            {
                next();
            }
            
        }while(inputSym.isSameType(TokenType.ident));

        if(inputSym.isSameType(TokenType.semiToken))
        {
            next();
        }
        else
        {
            error(new IncorrectSyntaxException("Semi comma not found in Variable Declaration."));
        }
    }

    private void formalParam(Function function, Boolean optimize)
    {
        if(inputSym.isSameType(TokenType.openparenToken))
        {
            next();
            while(inputSym.isSameType(TokenType.ident))
            {
                Variable v = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value), 
                                            Constants.FORMAL_PARAMETER_VERSION);
                VariableResult vResult = new VariableResult();
                vResult.set(v);
                try
                {
                    iCodeGenerator.declareVariable(function.head, function.vManager, vResult, false, optimize);
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
                        return;
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
                return;
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Open parenthesis not found while parsing formal parameters declaration."));
        }
    }

    private void funcDecl(Boolean optimize)
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
                    return;
                }
                function.head = (Block)cfg.initializeBlock();
                cfg.addFunction(function);

                next();
                if(inputSym.isSameType(TokenType.openparenToken))
                {
                    formalParam(function, optimize);
                }

                if(inputSym.isSameType(TokenType.semiToken))
                {
                    next();
                    funcBody(function, optimize);

                    if(inputSym.isSameType(TokenType.semiToken))
                    {
                        next();
                    }
                    else 
                    {
                        error(new IncorrectSyntaxException("Semi comma not found while parsing function declaration."));
                        return;
                    }
                }
                else
                {
                    error(new IncorrectSyntaxException("Semi comma not found while parsing function declaration."));
                    return;
                }
            }
        }
    }

    private void funcBody(Function function, Boolean optimize)
    {
        while(inputSym.isSameType(TokenType.varToken) || inputSym.isSameType(TokenType.arrToken))
        {
            varDecl(function, optimize);
        }

        if(inputSym.isSameType(TokenType.beginToken))
        {
            next();
            ArrayList<Instruction> kill = new ArrayList<Instruction>();
            function.tail = (Block)statSequence(function.head, function, optimize, kill);
            if(inputSym.isSameType(TokenType.endToken))
            {
                next();
            }
            else 
            {
                error(new IncorrectSyntaxException("End token not found while parsing function body."));
                return;
            }
        }
        else
        {
            error(new IncorrectSyntaxException("Begin token not found while parsing function body."));
            return;
        }
    }

    private boolean computation(Boolean optimize)
    {
        if(inputSym.isSameType(TokenType.mainToken))
        {
            next();

            while(inputSym.isSameType(TokenType.varToken) || inputSym.isSameType(TokenType.arrToken))
            {
                varDecl(null, optimize);
            }
            while(inputSym.isSameType(TokenType.funcToken) || inputSym.isSameType(TokenType.procToken))
            {
                funcDecl(optimize);
            }

            if(inputSym.isSameType(TokenType.beginToken))
            {
                next();
                ArrayList<Instruction> kill = new ArrayList<Instruction>();
                cfg.tail = (Block)statSequence(cfg.head, null, optimize, kill);
                if(cfg.tail == null)
                {
                    return false;
                }
                if(inputSym.isSameType(TokenType.endToken))
                {
                    next();
                    if(inputSym.isSameType(TokenType.periodToken))
                    {
                        Token opToken = inputSym;
                        next();
                        iCodeGenerator.compute(cfg.tail, opToken, null, null, optimize);
                        return true;
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

        return false;
    }

    public void error(Exception exception)
    {
        scanner.error(exception);
    }
}
