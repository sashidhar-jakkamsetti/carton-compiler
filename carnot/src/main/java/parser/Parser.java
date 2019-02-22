package parser;

import java.util.ArrayList;

import javax.lang.model.util.ElementScanner6;

import dataStructures.*;
import dataStructures.Blocks.*;
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

    private IResult designator(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
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

    private IResult factor(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IResult result = null;
        switch(inputSym.type)
        {
            case ident:
                result = designator(cBlock, jBlocks, function);
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
                result = expression(cBlock, jBlocks, function);
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
                result = funcCall(cBlock, jBlocks, function);
                break;
        }

        return result;
    }

    private IResult term(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IResult xResult = factor(cBlock, jBlocks, function);
        if(xResult != null)
        {
            while(inputSym.isTermOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = factor(cBlock, jBlocks, function);
                if(yResult != null)
                {
                    xResult.setIid(iCodeGenerator.getPC());
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }
        }

        return xResult;
    }

    private IResult expression(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IResult xResult = term(cBlock, jBlocks, function);
        if(xResult != null)
        {
            while(inputSym.isExpressionOp()) 
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = term(cBlock, jBlocks, function);
                if(yResult != null)
                {
                    xResult.setIid(iCodeGenerator.getPC());
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }    
        }

        return xResult;
    }

    private IResult relation(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IResult xResult = expression(cBlock, jBlocks, function);
        if(xResult != null)
        {
            while(inputSym.isRelationOp())
            {
                Token opToken = inputSym;
                next();
    
                IResult yResult = expression(cBlock, jBlocks, function);
                if(yResult != null)
                {
                    cBlock.addInstruction(iCodeGenerator.Compute(opToken, xResult, yResult));
                }
            }
        }

        return xResult;
    }

    private void assignment(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        if(inputSym.isSameType(TokenType.letToken))
        {
            IResult lhsResult = designator(cBlock, jBlocks, function);
            if(lhsResult != null)
            {
                if(inputSym.isSameType(TokenType.becomesToken))
                {
                    Token opToken = inputSym;
                    next();

                    IResult rhsResult = expression(cBlock, jBlocks, function);
                    if(rhsResult != null)
                    {
                        if(rhsResult.getIid() > 0) 
                        {
                            rhsResult = rhsResult.toInstruction();
                        }
                        Variable variable = ((VariableResult)lhsResult).variable;
                        variable.version = iCodeGenerator.getPC();

                        vManager.updateSsaMap(variable.address, variable.version);
                        vManager.updateDefUseChain(variable.address, variable.version, variable.version);
                        cBlock.addInstruction(iCodeGenerator.Compute(opToken, lhsResult, rhsResult));
                    }
                }
            }
        }
    }

    private IResult funcCall(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IResult fResult = null;
        if(inputSym.isSameType(TokenType.callToken))
        {
            next();

        }

        return fResult;
    }

    private IBlock ifStatement(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IBlock ifBlock = null;
        if(inputSym.isSameType(TokenType.ifToken))
        {
            next();


        }

        return ifBlock;
    }

    private IBlock whileStatement(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IBlock whileBlock = null;
        if(inputSym.isSameType(TokenType.whileToken))
        {
            next();

            
        }

        return whileBlock;
    }

    private IResult returnStatement(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IResult rResult = null;
        if(inputSym.isSameType(TokenType.returnToken))
        {
            next();
            rResult = expression(cBlock, jBlocks, function);
        }
        else
        {
            error(new IncorrectSyntaxException("Return token not found while parsing return statement."));
        }

        return rResult;
    }

    private IBlock statement(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IBlock block = null;
        if(inputSym.isSameType(TokenType.letToken))
        {
            assignment(cBlock, jBlocks, function);
            block = cBlock;
        }
        else if(inputSym.isSameType(TokenType.callToken))
        {
            funcCall(cBlock, jBlocks, function);
            block = cBlock;
        }
        else if(inputSym.isSameType(TokenType.ifToken))
        {
            block = ifStatement(cBlock, jBlocks, function);
        }
        else if(inputSym.isSameType(TokenType.whileToken))
        {
            block = whileStatement(cBlock, jBlocks, function);
        }
        else if(inputSym.isSameType(TokenType.returnToken))
        {
            IResult result = returnStatement(cBlock, jBlocks, function);
            if(result != null)
            {
                Token opToken = inputSym;
                InstructionResult iResult = new InstructionResult();
                iResult.set(iCodeGenerator.getPC());
                function.returnInstruction = iResult;
                cBlock.addInstruction(iCodeGenerator.Compute(opToken, result, iResult));
            }
            block = cBlock;
        }
        else
        {
            error(new IncorrectSyntaxException("No valid token found while parsing statement."));
        }

        return block;
    }

    private IBlock statSequence(IBlock cBlock, ArrayList<IBlock> jBlocks, Function function)
    {
        IBlock block = null;
        do
        {
            block = statement(cBlock, jBlocks, function);
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

    // TODO: array handling.
    private Array typeDecl()
    {
        Array arrayDec = new Array();
        if(inputSym.isSameType(TokenType.varToken))
        {
            next();
            arrayDec = null;
        }
        else if(inputSym.isSameType(TokenType.arrToken))
        {
            next();
            // Get Dimensions
        }

        return arrayDec;
    }

    // TODO: array handling. need to add support in VariableManager too.
    private void varDecl(Function function)
    {
        // while(inputSym.isSameType(TokenType.varToken) || inputSym.isSameType(TokenType.arrToken))
        // {
        //     Array arrayDec = typeDecl();
        //     if(inputSym.isSameType(TokenType.ident))
        //     {
        //         do
        //         {
        //             Variable v = new Variable(inputSym.value, scanner.identifier2Address.get(inputSym.value));
        //             VariableResult vResult = new VariableResult();
        //             vResult.set(v);
        //             vResult.setIid(iCodeGenerator.getPC());
        
        //             try
        //             {
        //                 if(cfg.cfunction == null)
        //                 {
        //                     iCodeGenerator.declareVariable(cfg.current, vManager, vResult);
        //                 }
        //                 else
        //                 {
        //                     iCodeGenerator.declareVariable(cfg.current, cfg.cfunction.vManager, vResult);
        //                 }
        //                 next();
        //             }
        //             catch(IllegalVariableException e)
        //             {
        //                 error(e);
        //             }
                    
        //             if(inputSym.isSameType(TokenType.commaToken))
        //             {
        //                 next();
        //             }
        //             else
        //             {
        //                 break;
        //             }
        //         }while(inputSym.isSameType(TokenType.ident));
        //     }
        //     else 
        //     {
        //         error(new IncorrectSyntaxException("Identifier in variable/array declaration not found while parsing variable declaration."));
        //     }

        //     if(inputSym.isSameType(TokenType.semiToken))
        //     {
        //         next();
        //     }
        //     else
        //     {
        //         error(new IncorrectSyntaxException("Semi comma not found while parsing variable declaration."));
        //     }
        // }
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
                try
                {
                    iCodeGenerator.declareVariable(function.head, function.vManager, vResult);
                }
                catch(IllegalVariableException e)
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
            statSequence(function.head, null, function);
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
                IBlock lBlock = statSequence(cfg.head, null, null);
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
