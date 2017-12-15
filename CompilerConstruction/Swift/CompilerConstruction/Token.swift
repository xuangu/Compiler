//
//  Token.swift
//  CompilerConstruction
//
//  Created by guxuan on 04/12/2017.
//  Copyright © 2017 gu. All rights reserved.
//

import Foundation

class Token {
    private var index: Int
    private var input: String
    
    init(str: String) {
        input = str
        index = 0
    }
    
    public func getNextToken() -> Character {
        if index < input.count {
            let c = input[input.index(input.startIndex, offsetBy: index)]
            index += 1
            
            return c
        }
        
        return "\0"
    }
    
    
}
