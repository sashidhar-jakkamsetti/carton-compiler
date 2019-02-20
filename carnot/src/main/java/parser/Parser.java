package parser;

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
                    xResult.setIid(iCodeGenerator.getPC());
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
                    xResult.setIid(iCodeGenerator.getPC());
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
                        if(rhsResult.getIid() > 0) 
                        {
                            rhsResult = rhsResult.toInstruction();
                        }
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

    private void ifStatement()
    {
        if(inputSym.isSameType(TokenType.ifToken))
        {
            next();
            IBlock temp = cfg.current;
            cfg.initializeIfBlock();
            temp.setChild(cfg.current);
            cfg.current.setParent(temp);
            IBlock ifBlock = cfg.current;
            cfg.initializeBlock();
            IBlock followBlock = cfg.current;
            cfg.current = ifBlock;


        }
    }

    private IBlock whileStatement()
    {
        IBlock whileBlock = null;
        if(inputSym.isSameType(TokenType.whileToken))
        {
            next();

            
        }

        return whileBlock;
    }

    private IResult returnStatement()
    {
        IResult rResult = null;
        if(inputSym.isSameType(TokenType.returnToken))
        {
            next();
            rResult = expression();
        }
        else
        {
            error(new IncorrectSyntaxException("Return token not found while parsing return statement."));
        }

        return rResult;
    }

    private void statement()
    {
        if(inputSym.isSameType(TokenType.letToken))
        {
            assignment();
        }
        else if(inputSym.isSameType(TokenType.callToken))
        {
            funcCall();
        }
        else if(inputSym.isSameType(TokenType.ifToken))
        {
            ifStatement();
        }
        else if(inputSym.isSameType(TokenType.whileToken))
        {
            whileStatement();
        }
        else if(inputSym.isSameType(TokenType.returnToken))
        {
            IResult result = returnStatement();
            if(result != null)
            {
                Token opToken = inputSym;
                InstructionResult iResult = new InstructionResult();
                iResult.set(iCodeGenerator.getPC());
                cfg.cfunction.returnInstruction = iResult;
                cfg.current.addInstruction(iCodeGenerator.Compute(opToken, result, iResult));
            }
        }
    }

    private void statSequence()
    {
        do
        {
            statement();
            if(inputSym.isSameType(TokenType.semiToken))
            {
                next();
            }
            else
            {
                break;
            }
        }while(true);
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
    private void varDecl()
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

    private void formalParam()
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
                    iCodeGenerator.declareVariable(cfg.current, cfg.cfunction.vManager, vResult);
                }
                catch(IllegalVariableException e)
                {
                    error(e);
                }
                cfg.cfunction.addParameter(vResult);
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
                cfg.initializeBlock();
                cfg.cfunction = function;
                cfg.cfunction.head = (Block)cfg.current;
                cfg.cfunction.setGlobalVariables(vManager.getVariables());
                formalParam();

                if(inputSym.isSameType(TokenType.semiToken))
                {
                    next();
                    funcBody();

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

    private void funcBody()
    {
        varDecl();

        if(inputSym.isSameType(TokenType.beginToken))
        {
            statSequence();
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
            varDecl();
            funcDecl();

            if(inputSym.isSameType(TokenType.beginToken))
            {
                statSequence();
                if(inputSym.isSameType(TokenType.endToken))
                {
                    next();
                    if(inputSym.isSameType(TokenType.periodToken))
                    {
                        Token opToken = inputSym;
                        next();
                        cfg.current.addInstruction(iCodeGenerator.Compute(opToken, null, null));
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
