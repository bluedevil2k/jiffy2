package org.jiffy.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class Dollar implements Comparable<Dollar>, Serializable
{
	public static final Dollar ZERO = new Dollar("0.00");
	
	private static final long serialVersionUID = 893832762897563929L;

	protected BigDecimal amount;
	protected Currency currency;
	protected RoundingMode roundingMode;

	// in situations that carry more than 2 decimal spots for cents
	// like in tax situations or currency trading
	public Dollar(BigDecimal dollarAmount, int centDigits)
	{
		this.amount = dollarAmount.setScale(centDigits, RoundingMode.HALF_EVEN);
		this.currency = Currency.getInstance("USD");
	}
		
	public Dollar(BigDecimal dollarAmount)
	{
		this(dollarAmount, 2);
	}
	
	public Dollar(String amount)
	{
		 this(new BigDecimal(amount));
	}
	
	public BigDecimal getAmount()
	{
		return amount;
	}
	
	public double doubleValue()
	{
		return amount.doubleValue();
	}
	
	public Dollar plus(Dollar d)
	{
		return new Dollar(amount.add(d.getAmount()));
	}
	  
	public Dollar minus(Dollar d)
	{
		return new Dollar(amount.subtract(d.getAmount()));
	}
	
	public static Dollar sum(Dollar...d)
	{
		Dollar sum = ZERO;
		for (int i=0; i<d.length; i++)
		{
			sum = sum.plus(d[i]);
		}
		return sum;
	}

	public Dollar times(double multiple)
	{
		return new Dollar(amount.multiply(new BigDecimal(multiple)));	
	}

	public Dollar divide(double dividend)
	{
		return new Dollar(amount.divide(new BigDecimal(dividend)));
	}
	  
	public Dollar afterTax(double taxRate)	
	{
		return times(1 + taxRate);
	}
	  
	// return % in decimal terms, e.g. 0.825 or 1.35
	public static double percentDifference(Dollar d1, Dollar d2, int decimal)
	{
		return d1.getAmount().divide(d2.getAmount(), decimal, RoundingMode.HALF_UP).doubleValue();
	}
	
	public boolean isGreaterThan(Dollar d)
	{
		return amount.compareTo(d.getAmount()) > 0;
	}
	
	public boolean isGreaterThanEquals(Dollar d)
	{
		return amount.compareTo(d.getAmount()) >= 0;
	}
	
	public boolean isLessThan(Dollar d)
	{
		return amount.compareTo(d.getAmount()) < 0;
	}

	public boolean isLessThanEquals(Dollar d)
	{
		return amount.compareTo(d.getAmount()) <= 0;
	}
	
	public boolean isPositive()
	{
		return isGreaterThan(ZERO);
	}
	
	public boolean isNegative()
	{
		return isLessThan(ZERO);
	}
	
	public boolean isZero()
	{
		return equals(ZERO);
	}
	
	public Dollar inverse()
	{
		return times(-1);
	}
	
	public Dollar roundToNearest(Dollar d)
	{
		return new Dollar(new BigDecimal(MathUtil.roundToNearest(amount.doubleValue(), d.doubleValue())));
	}
	
	public Dollar roundToSigDigits(int sigDigits)
	{
		return new Dollar(new BigDecimal(MathUtil.roundToSigDigits(amount.doubleValue(), sigDigits, RoundingMode.HALF_EVEN.ordinal())));
	}
	
	public String toString()
	{
		return currency.getSymbol() + amount.toPlainString();
	}
	
	public String toString(String format)
	{
		DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance(Locale.getDefault());
		formatter.applyPattern(format);
		return formatter.format(doubleValue());
	}
	
	public static Dollar parseDollar(String dollarString) throws Exception
	{
	    if (StringUtils.isEmpty(dollarString))
	    {
			return ZERO;
	    }
	    
		double unrounded = 0.0;
		DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance(Locale.getDefault());
		// the price one isn't very tolerant when the currency is excluded, so for this we try it with the currency
		// one in case they did type it, and then fail to a normal one if they didn't type it, and then fail
		// to a thrown exception
		try
		{
			formatter.applyPattern("$#,###.00");
			unrounded = formatter.parse(dollarString.trim()).doubleValue();
		}
		catch (Exception ex)
		{
			try
			{
				formatter.applyPattern("#,###.00");
				unrounded = formatter.parse(dollarString.trim()).doubleValue();
			}
			catch (Exception e) 
			{ 
				throw new Exception(Text.get("error.invalidPriceFormat"));
			}
		}

		return new Dollar(new BigDecimal(unrounded));
	}

	  public int compareTo(Dollar d) 
	  {
	    return amount.compareTo(d.getAmount());
	  }
	  
	  public boolean equals(Dollar d)
	  {
		  return amount.equals(d.getAmount());
	  }
}

