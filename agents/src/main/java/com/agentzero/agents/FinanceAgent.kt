package com.agentzero.agents

import com.agentzero.core.*
import timber.log.Timber

/**
 * Finance-focused AI agent
 * 
 * Specializes in:
 * - Budget tracking and analysis
 * - Expense categorization
 * - Financial goal setting
 * - Investment guidance
 * - Spending pattern analysis
 */
class FinanceAgent(
    name: String,
    voiceProfile: VoiceProfile
) : Agent(
    name = name,
    type = "finance",
    voiceProfile = voiceProfile
) {
    
    override val tools: List<AgentTool> = listOf()
    
    override val personality = AgentPersonality(
        traits = mapOf(
            "analytical" to 0.9f,
            "prudent" to 0.8f,
            "helpful" to 0.8f,
            "detail_oriented" to 0.9f,
            "trustworthy" to 0.9f
        ),
        communicationStyle = "professional_advisor",
        responseLength = "medium",
        expertise = listOf(
            "personal_finance",
            "budgeting",
            "investment_basics",
            "expense_tracking",
            "financial_planning",
            "debt_management"
        ),
        limitations = listOf(
            "Not a licensed financial advisor",
            "Cannot provide specific investment advice",
            "Recommendations are general financial education"
        )
    )
    
    override val knowledgeDomains = listOf(
        "finance",
        "budgeting",
        "investing",
        "economics",
        "personal_finance",
        "financial_planning"
    )
    
    override suspend fun generateResponse(input: String, context: AgentContext): String {
        val inputLower = input.lowercase()
        
        return when {
            // Budget and spending queries
            inputLower.contains("budget") || inputLower.contains("spend") || inputLower.contains("expense") -> {
                handleBudgetQuery(input, context)
            }
            
            // Saving and goals
            inputLower.contains("save") || inputLower.contains("goal") || inputLower.contains("emergency fund") -> {
                handleSavingsQuery(input, context)
            }
            
            // Investment queries
            inputLower.contains("invest") || inputLower.contains("stock") || inputLower.contains("portfolio") -> {
                handleInvestmentQuery(input, context)
            }
            
            // Debt and credit
            inputLower.contains("debt") || inputLower.contains("credit") || inputLower.contains("loan") -> {
                handleDebtQuery(input, context)
            }
            
            // Income and earnings
            inputLower.contains("income") || inputLower.contains("salary") || inputLower.contains("earn") -> {
                handleIncomeQuery(input, context)
            }
            
            // Financial planning
            inputLower.contains("plan") || inputLower.contains("retire") || inputLower.contains("future") -> {
                handlePlanningQuery(input, context)
            }
            
            else -> {
                handleGeneralQuery(input, context)
            }
        }
    }
    
    private suspend fun handleBudgetQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Let's talk about budgeting - the foundation of financial wellness! ")
            
            append("A good budget helps you understand where your money goes and ensures you're spending intentionally. ")
            
            append("\n\nHere's a simple budgeting framework to get started:\n")
            append("• 50% for needs (housing, utilities, groceries, minimum debt payments)\n")
            append("• 30% for wants (dining out, entertainment, hobbies)\n")
            append("• 20% for savings and extra debt payments\n")
            
            append("\nBudgeting steps:\n")
            append("1. Track your income (after taxes)\n")
            append("2. List all your expenses for the past month\n")
            append("3. Categorize expenses as needs vs wants\n")
            append("4. Compare your spending to the 50/30/20 rule\n")
            append("5. Adjust as needed to meet your goals\n")
            
            append("\nPro tips:\n")
            append("• Use the envelope method for discretionary spending\n")
            append("• Review and adjust your budget monthly\n")
            append("• Automate savings so you pay yourself first\n")
            append("• Track expenses in real-time with apps or receipts\n")
            
            append("\nRemember: A budget isn't about restricting yourself - it's about giving every dollar a purpose!")
        }
    }
    
    private suspend fun handleSavingsQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Saving money is one of the smartest financial moves you can make! ")
            
            append("Let me help you build a solid savings strategy. ")
            
            append("\n\nSavings priorities (in order):\n")
            append("1. Emergency fund: Start with $1,000, then build to 3-6 months of expenses\n")
            append("2. Employer 401(k) match: Free money - always take the full match\n")
            append("3. High-interest debt: Pay off credit cards and personal loans\n")
            append("4. Retirement savings: Aim for 10-15% of income\n")
            append("5. Other goals: House down payment, vacation, etc.\n")
            
            append("\nSaving strategies that work:\n")
            append("• Automate transfers to savings right after payday\n")
            append("• Use the 'pay yourself first' principle\n")
            append("• Save windfalls (tax refunds, bonuses) immediately\n")
            append("• Try the 52-week challenge: save $1 week 1, $2 week 2, etc.\n")
            append("• Round up purchases and save the change\n")
            
            append("\nWhere to keep your savings:\n")
            append("• Emergency fund: High-yield savings account (liquid, safe)\n")
            append("• Short-term goals (< 2 years): High-yield savings or CDs\n")
            append("• Long-term goals (> 5 years): Consider investing\n")
            
            append("\nRemember: Start small if needed - even $25/month builds the habit!")
        }
    }
    
    private suspend fun handleInvestmentQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Investing is how you build long-term wealth! Let me share some fundamental principles. ")
            
            append("\n\nInvesting basics:\n")
            append("• Time in the market beats timing the market\n")
            append("• Diversification reduces risk\n")
            append("• Low fees matter more than you think\n")
            append("• Dollar-cost averaging smooths out volatility\n")
            append("• Don't invest money you'll need within 5 years\n")
            
            append("\nBeginner-friendly investment options:\n")
            append("• Target-date funds: Automatically adjusts as you age\n")
            append("• Index funds: Low-cost, diversified, tracks market performance\n")
            append("• ETFs: Like index funds but trade like stocks\n")
            append("• Robo-advisors: Automated portfolio management\n")
            
            append("\nInvestment accounts to consider:\n")
            append("• 401(k): Employer-sponsored, often with matching\n")
            append("• IRA: Individual retirement account, tax advantages\n")
            append("• Roth IRA: Tax-free growth and withdrawals in retirement\n")
            append("• Taxable brokerage: For goals beyond retirement\n")
            
            append("\nRisk and return:\n")
            append("• Stocks: Higher risk, higher potential return (long-term)\n")
            append("• Bonds: Lower risk, lower return, provides stability\n")
            append("• Mix both based on your age and risk tolerance\n")
            
            append("\nIMPORTANT: This is educational information only. Consider consulting with a licensed financial advisor for personalized investment advice.")
        }
    }
    
    private suspend fun handleDebtQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Let's tackle debt strategically! Debt can be a tool or a burden - it depends on how you manage it. ")
            
            append("\n\nDebt payoff strategies:\n")
            append("• Debt Snowball: Pay minimums on all debts, extra on smallest balance\n")
            append("  - Pros: Quick wins, psychological motivation\n")
            append("  - Best for: People who need motivation\n")
            
            append("• Debt Avalanche: Pay minimums on all debts, extra on highest interest rate\n")
            append("  - Pros: Saves more money mathematically\n")
            append("  - Best for: People motivated by efficiency\n")
            
            append("\nDebt management tips:\n")
            append("• List all debts: balance, minimum payment, interest rate\n")
            append("• Stop using credit cards while paying off debt\n")
            append("• Consider debt consolidation if it lowers your rate\n")
            append("• Negotiate with creditors if you're struggling\n")
            append("• Build a small emergency fund ($1,000) even while paying off debt\n")
            
            append("\nGood debt vs bad debt:\n")
            append("• Good debt: Mortgages, student loans (reasonable amounts)\n")
            append("• Bad debt: Credit cards, payday loans, car loans (usually)\n")
            
            append("\nCredit score improvement:\n")
            append("• Pay all bills on time (35% of score)\n")
            append("• Keep credit utilization below 30% (30% of score)\n")
            append("• Don't close old credit cards\n")
            append("• Check your credit report annually for errors\n")
            
            append("\nRemember: Every payment gets you closer to financial freedom!")
        }
    }
    
    private suspend fun handleIncomeQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Income is your most powerful wealth-building tool! Let's maximize it. ")
            
            append("\n\nWays to increase income:\n")
            append("• Negotiate your salary: Research market rates, document achievements\n")
            append("• Develop high-value skills: Technology, communication, leadership\n")
            append("• Side hustles: Freelancing, tutoring, selling products/services\n")
            append("• Passive income: Rental properties, dividends, royalties\n")
            append("• Career advancement: Additional certifications, networking\n")
            
            append("\nSalary negotiation tips:\n")
            append("• Research salary ranges for your role and location\n")
            append("• Document your achievements and value-add\n")
            append("• Practice your pitch beforehand\n")
            append("• Consider the total compensation package\n")
            append("• Be prepared to walk away if needed\n")
            
            append("\nSide hustle ideas:\n")
            append("• Freelance your professional skills\n")
            append("• Teach or tutor in your area of expertise\n")
            append("• Sell products online (crafts, digital products)\n")
            append("• Provide services (cleaning, pet sitting, delivery)\n")
            append("• Rent out assets (car, parking space, room)\n")
            
            append("\nIncome optimization:\n")
            append("• Maximize employer benefits (401k match, HSA, etc.)\n")
            append("• Understand your tax situation and optimize\n")
            append("• Invest in yourself through education and skills\n")
            append("• Build multiple income streams for security\n")
            
            append("\nRemember: Your income is your greatest wealth-building asset - invest in growing it!")
        }
    }
    
    private suspend fun handlePlanningQuery(input: String, context: AgentContext): String {
        return buildString {
            append("Financial planning is about creating a roadmap for your financial future! ")
            
            append("\n\nFinancial planning steps:\n")
            append("1. Define your goals: Short-term (< 2 years), medium-term (2-10 years), long-term (> 10 years)\n")
            append("2. Assess your current situation: Net worth, cash flow, debt\n")
            append("3. Create a budget and savings plan\n")
            append("4. Build an emergency fund\n")
            append("5. Optimize insurance coverage\n")
            append("6. Start investing for long-term goals\n")
            append("7. Plan for retirement\n")
            append("8. Consider estate planning\n")
            
            append("\nRetirement planning basics:\n")
            append("• Start early - compound interest is powerful\n")
            append("• Aim to save 10-15% of income for retirement\n")
            append("• Take advantage of employer 401(k) matching\n")
            append("• Consider both traditional and Roth accounts\n")
            append("• Estimate you'll need 70-90% of pre-retirement income\n")
            
            append("\nFinancial milestones by age (rough guidelines):\n")
            append("• 20s: Build emergency fund, start retirement savings\n")
            append("• 30s: Have 1x annual salary saved for retirement\n")
            append("• 40s: Have 3x annual salary saved for retirement\n")
            append("• 50s: Have 5x annual salary saved for retirement\n")
            append("• 60s: Have 8-10x annual salary saved for retirement\n")
            
            append("\nKey financial ratios to track:\n")
            append("• Savings rate: Aim for 20% of gross income\n")
            append("• Debt-to-income: Keep total debt payments under 36% of income\n")
            append("• Emergency fund: 3-6 months of expenses\n")
            
            append("\nRemember: Financial planning is a marathon, not a sprint. Start where you are and keep moving forward!")
        }
    }
    
    private suspend fun handleGeneralQuery(input: String, context: AgentContext): String {
        return "Hello! I'm Alex, your personal finance advisor. I'm here to help you make smart money decisions, whether that's creating a budget, planning for goals, understanding investments, or optimizing your financial strategy. I believe everyone can achieve financial wellness with the right knowledge and habits. What financial topic would you like to explore today?"
    }
    
    override suspend fun onOtherAgentResponse(otherAgentId: String, response: String) {
        // Finance agent can coordinate with other agents for holistic advice
        Timber.d("Finance agent received input from $otherAgentId: ${response.take(50)}...")
        
        // Could provide financial implications of health/fitness recommendations
        // Or coordinate spending advice based on wellness goals
    }
}